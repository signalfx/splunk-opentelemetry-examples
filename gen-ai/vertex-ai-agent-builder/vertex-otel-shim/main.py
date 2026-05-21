"""Cloud Run shim: Pub/Sub push → OTel spans → OTLP backend."""
from __future__ import annotations

import base64
import json
import logging
import os
from typing import Any

from flask import Flask, request

from opentelemetry.sdk.resources import Resource
from opentelemetry.sdk.trace import TracerProvider, ReadableSpan
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter
from opentelemetry.trace import SpanContext, TraceFlags, SpanKind, Status, StatusCode
from opentelemetry.util.types import Attributes

from converter import convert_log_to_spans, SynthSpan

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# --- OTel pipeline setup -----------------------------------------------------

OTLP_ENDPOINT = os.environ.get("OTLP_ENDPOINT", "http://localhost:4318/v1/traces")
OTLP_HEADERS_RAW = os.environ.get("OTLP_HEADERS", "")  # "key1=val1,key2=val2"

def _parse_headers(raw: str) -> dict[str, str]:
    out = {}
    for pair in raw.split(","):
        pair = pair.strip()
        if "=" in pair:
            k, v = pair.split("=", 1)
            out[k.strip()] = v.strip()
    return out

resource = Resource.create({
    "service.name": os.environ.get("OTEL_SERVICE_NAME", "vertex-conversational-agent"),
    "service.namespace": os.environ.get("OTEL_SERVICE_NAMESPACE", "ai-agents"),
    "gen_ai.system": "vertex_ai",
    "cloud.provider": "gcp",
})

provider = TracerProvider(resource=resource)
exporter = OTLPSpanExporter(
    endpoint=OTLP_ENDPOINT,
    headers=_parse_headers(OTLP_HEADERS_RAW),
)
span_processor = BatchSpanProcessor(exporter)
provider.add_span_processor(span_processor)

# --- Span emission helper ----------------------------------------------------

def _emit_synth_span(s: SynthSpan) -> None:
    """Push a synthesized span directly into the OTel pipeline.

    We bypass the normal tracer.start_span() path because we need to set
    a parent context, span IDs, and timestamps that come from external data.
    """
    parent_context = None
    if s.parent_span_id is not None:
        parent_context = SpanContext(
            trace_id=s.trace_id,
            span_id=s.parent_span_id,
            is_remote=False,
            trace_flags=TraceFlags(TraceFlags.SAMPLED),
        )

    span_context = SpanContext(
        trace_id=s.trace_id,
        span_id=s.span_id,
        is_remote=False,
        trace_flags=TraceFlags(TraceFlags.SAMPLED),
    )

    # Filter None-valued attributes (OTLP rejects them)
    clean_attrs: dict[str, Any] = {
        k: v for k, v in s.attributes.items() if v is not None
    }

    status = Status(StatusCode.OK if s.status_ok else StatusCode.ERROR, s.status_message)

    readable = ReadableSpan(
        name=s.name,
        context=span_context,
        parent=parent_context,
        resource=resource,
        attributes=clean_attrs,
        events=[],  # we'll add below if any
        links=[],
        kind=SpanKind.INTERNAL,
        instrumentation_scope=None,
        status=status,
        start_time=s.start_ns,
        end_time=s.end_ns,
    )

    # Attach events if any
    if s.events:
        from opentelemetry.sdk.trace import Event
        evs = [
            Event(
                name=e["name"],
                attributes=e.get("attributes", {}),
                timestamp=e.get("timestamp_ns", s.start_ns),
            )
            for e in s.events
        ]
        # ReadableSpan doesn't accept events in constructor in all SDK versions;
        # we set the private attr, which BatchSpanProcessor reads on export.
        readable._events = evs

    span_processor.on_end(readable)

# --- Flask app ---------------------------------------------------------------

app = Flask(__name__)


@app.route("/", methods=["POST"])
def pubsub_push() -> tuple[str, int]:
    envelope = request.get_json(silent=True)
    if not envelope or "message" not in envelope:
        logger.warning("Bad Pub/Sub envelope: %s", envelope)
        return ("Bad Request", 400)

    msg = envelope["message"]
    data_b64 = msg.get("data", "")
    if not data_b64:
        # Empty message — ack so Pub/Sub doesn't retry forever.
        return ("", 204)

    try:
        raw = base64.b64decode(data_b64).decode("utf-8")
        log_entry = json.loads(raw)
    except Exception as e:
        logger.exception("Failed to decode Pub/Sub message: %s", e)
        # Ack to avoid infinite retry on malformed data.
        return ("", 204)

    try:
        spans = convert_log_to_spans(log_entry)
    except Exception as e:
        logger.exception("Conversion error: %s", e)
        return ("", 204)  # Ack — don't redeliver bad logs

    for span in spans:
        try:
            _emit_synth_span(span)
        except Exception as e:
            logger.exception("Failed to emit span %s: %s", span.name, e)

    logger.info(
        "Emitted %d spans for response_id=%s session_id=%s",
        len(spans),
        log_entry.get("jsonPayload", {}).get("responseId", "?"),
        log_entry.get("labels", {}).get("session_id", "?"),
    )
    return ("", 204)


@app.route("/healthz", methods=["GET"])
def healthz() -> tuple[str, int]:
    return ("ok", 200)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.environ.get("PORT", 8080)))