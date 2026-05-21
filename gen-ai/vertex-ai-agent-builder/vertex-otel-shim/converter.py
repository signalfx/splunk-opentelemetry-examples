"""Convert Vertex Conversational Agent logs to OpenTelemetry spans."""
from __future__ import annotations

import hashlib
import json
import logging
from dataclasses import dataclass, field
from datetime import datetime
from typing import Any, Optional

logger = logging.getLogger(__name__)


@dataclass
class SynthSpan:
    """A span synthesized from log data, ready to be exported."""
    name: str
    trace_id: int
    span_id: int
    parent_span_id: Optional[int]
    start_ns: int
    end_ns: int
    attributes: dict[str, Any] = field(default_factory=dict)
    events: list[dict[str, Any]] = field(default_factory=list)
    status_ok: bool = True
    status_message: str = ""


def _parse_ts(s: str) -> int:
    """Parse RFC3339 timestamp to nanoseconds since epoch."""
    # Handle nanosecond precision that fromisoformat can't handle directly
    if "." in s:
        base, frac = s.split(".")
        frac = frac.rstrip("Z").rstrip("+00:00")
        # Truncate to microseconds for fromisoformat compatibility
        frac_us = (frac + "000000")[:6]
        s_clean = f"{base}.{frac_us}+00:00"
    else:
        s_clean = s.replace("Z", "+00:00")
    dt = datetime.fromisoformat(s_clean)
    return int(dt.timestamp() * 1_000_000_000)


def _hash_id(seed: str, bits: int) -> int:
    """Deterministically generate a trace/span ID from a seed string."""
    h = hashlib.sha256(seed.encode()).hexdigest()
    return int(h[: bits // 4], 16)


def _trace_id(seed: str) -> int:
    return _hash_id(seed, 128)


def _span_id(seed: str) -> int:
    return _hash_id(seed, 64)


def _safe_int(v: Any) -> Optional[int]:
    try:
        return int(v)
    except (TypeError, ValueError):
        return None


def _truncate(s: str, limit: int = 8000) -> str:
    if len(s) <= limit:
        return s
    return s[:limit] + f"... [truncated {len(s) - limit} chars]"


def convert_log_to_spans(log_entry: dict[str, Any]) -> list[SynthSpan]:
    """Convert a single Cloud Logging entry into a list of OTel spans."""
    payload = log_entry.get("jsonPayload") or {}
    qr = payload.get("queryResult") or {}
    response_id = payload.get("responseId") or log_entry.get("insertId", "unknown")

    labels = log_entry.get("labels") or {}
    session_id = labels.get("session_id", "unknown")
    agent_id = labels.get("agent_id", "unknown")
    location_id = labels.get("location_id", "unknown")
    project_id = (log_entry.get("resource", {}).get("labels", {}).get("project_id", "unknown"))

    base_attrs: dict[str, Any] = {
        "session.id": session_id,
        "vertex.response.id": response_id,
        "vertex.agent.id": agent_id,
        "cloud.account.id": project_id,
        "cloud.region": location_id,
        "gen_ai.system": "vertex_ai",
        "vertex.language_code": qr.get("languageCode", ""),
    }

    match = qr.get("match", {})
    if match:
        base_attrs["vertex.match.type"] = match.get("matchType", "")
        base_attrs["vertex.match.confidence"] = match.get("confidence", 0)

    trace_id = _trace_id(response_id)
    spans: list[SynthSpan] = []

    trace_blocks = qr.get("traceBlocks") or []
    if not trace_blocks:
        logger.info("No traceBlocks in log entry %s; skipping", response_id)
        return spans

    # Compute the overall turn span as a parent over all trace blocks.
    turn_start = min(_parse_ts(tb["startTime"]) for tb in trace_blocks if "startTime" in tb)
    turn_end = max(_parse_ts(tb["completeTime"]) for tb in trace_blocks if "completeTime" in tb)
    turn_span_id = _span_id(f"{response_id}/turn")

    user_text = qr.get("text", "")
    response_text = ""
    for rm in qr.get("responseMessages", []) or []:
        text_obj = rm.get("text", {})
        for t in text_obj.get("text", []) or []:
            response_text = t
            break
        if response_text:
            break

    input_messages = [
        {
            "role": "user",
            "parts": [
                {
                    "type": "text",
                    "content": user_text,
                }
            ],
        },
    ]

    output_messages = [
        {
            "role": "assistant",
            "finish_reason": "stop",
            "parts": [
                {
                    "type": "text",
                    "content": response_text,
                }
            ],
        },
    ]

    spans.append(SynthSpan(
        name="conversation.turn",
        trace_id=trace_id,
        span_id=turn_span_id,
        parent_span_id=None,
        start_ns=turn_start,
        end_ns=turn_end,
        attributes={
            **base_attrs,
            "gen_ai.operation.name": "chat",
            "vertex.user.text": _truncate(user_text, 1000),
            "vertex.response.text": _truncate(response_text, 1000),
            "gen_ai.input.messages": input_messages,
            "gen_ai.output.messages": output_messages,
            "vertex.llm_calls": payload.get("ulmCalls", 0),
        },
    ))

    for tb_idx, tb in enumerate(trace_blocks):
        playbook_meta = tb.get("playbookTraceMetadata", {})
        playbook_name = playbook_meta.get("displayName", "unknown")
        playbook_span_id = _span_id(f"{response_id}/tb/{tb_idx}")

        spans.append(SynthSpan(
            name=f"playbook.execute {playbook_name}",
            trace_id=trace_id,
            span_id=playbook_span_id,
            parent_span_id=turn_span_id,
            start_ns=_parse_ts(tb["startTime"]),
            end_ns=_parse_ts(tb["completeTime"]),
            attributes={
                **base_attrs,
                "vertex.playbook.name": playbook_name,
                "vertex.playbook.id": playbook_meta.get("playbook", ""),
                "vertex.playbook.end_state": tb.get("endState", ""),
            },
        ))

        for a_idx, action in enumerate(tb.get("actions", []) or []):
            action_span_id = _span_id(f"{response_id}/tb/{tb_idx}/a/{a_idx}")
            display = action.get("displayName", "action")

            try:
                start_ns = _parse_ts(action["startTime"])
                end_ns = _parse_ts(action["completeTime"])
            except (KeyError, ValueError):
                logger.warning("Skipping action %d with bad timestamps", a_idx)
                continue

            attrs = dict(base_attrs)
            name = display.lower()

            if "llmCall" in action:
                llm = action["llmCall"]
                model = llm.get("model", "unknown")
                tc = llm.get("tokenCount", {})
                name = f"llm.call {model}"
                attrs.update({
                    "gen_ai.operation.name": "chat",
                    "gen_ai.request.model": model,
                    "gen_ai.request.temperature": llm.get("temperature"),
                    "gen_ai.usage.input_tokens": _safe_int(tc.get("totalInputTokenCount")),
                    "gen_ai.usage.output_tokens": _safe_int(tc.get("totalOutputTokenCount")),
                    "vertex.llm.context_tokens": _safe_int(tc.get("conversationContextTokenCount")),
                    "vertex.llm.retrieved_examples": len(llm.get("retrievedExamples", []) or []),
                })

            elif "toolUse" in action:
                tu = action["toolUse"]
                tool_action = tu.get("action", "unknown")
                name = f"tool.use {tool_action}"
                attrs.update({
                    "gen_ai.tool.name": tool_action,
                    "gen_ai.tool.type": tu.get("displayName", ""),
                    "vertex.tool.id": tu.get("tool", ""),
                    "vertex.tool.input": _truncate(json.dumps(tu.get("inputActionParameters", {}))),
                    "vertex.tool.output": _truncate(json.dumps(tu.get("outputActionParameters", {}))),
                })

            elif "userUtterance" in action:
                name = "user.utterance"
                attrs["vertex.utterance.text"] = _truncate(
                    action["userUtterance"].get("text", ""), 1000
                )

            elif "agentUtterance" in action:
                name = "agent.utterance"
                attrs["vertex.utterance.text"] = _truncate(
                    action["agentUtterance"].get("text", ""), 1000
                )

            spans.append(SynthSpan(
                name=name,
                trace_id=trace_id,
                span_id=action_span_id,
                parent_span_id=playbook_span_id,
                start_ns=start_ns,
                end_ns=end_ns,
                attributes=attrs,
            ))

            # Sub-execution steps (e.g., code_block_execution)
            for s_idx, step in enumerate(action.get("subExecutionSteps", []) or []):
                step_attrs = dict(base_attrs)
                step_events = []
                for m in step.get("metrics", []) or []:
                    mname = m.get("name")
                    mval = m.get("value")
                    if m.get("unit") == "ms" and isinstance(mval, (int, float)):
                        step_attrs[f"vertex.metric.{mname}_ms"] = mval
                    elif mname == "debug_log" and isinstance(mval, str):
                        step_events.append({
                            "name": "debug_log",
                            "timestamp_ns": _parse_ts(step["startTime"]),
                            "attributes": {"log": _truncate(mval, 8000)},
                        })
                    elif mname == "tool_calls":
                        step_attrs["vertex.metric.tool_calls"] = json.dumps(mval)

                spans.append(SynthSpan(
                    name=step.get("name", "sub_execution_step"),
                    trace_id=trace_id,
                    span_id=_span_id(f"{response_id}/tb/{tb_idx}/a/{a_idx}/s/{s_idx}"),
                    parent_span_id=action_span_id,
                    start_ns=_parse_ts(step["startTime"]),
                    end_ns=_parse_ts(step["completeTime"]),
                    attributes=step_attrs,
                    events=step_events,
                ))

    return spans