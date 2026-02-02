from langchain_core.messages import HumanMessage
from langchain.agents import (
    create_agent as _create_react_agent,  # type: ignore[attr-defined]
)
from langchain.tools import tool
from bedrock_agentcore import BedrockAgentCoreApp
from mcp_client.client import get_streamable_http_mcp_client
from model.load import load_model

from opentelemetry import trace
from opentelemetry.instrumentation.langchain import LangChainInstrumentor

from splunk_otel import init_splunk_otel
init_splunk_otel()

tracer = trace.get_tracer("agentcore-example-tracer")
LangChainInstrumentor().instrument()

# Define a simple function tool
@tool
def add_numbers(a: int, b: int) -> int:
    """Return the sum of two numbers"""
    return a+b

# Import AgentCore Gateway as Streamable HTTP MCP Client
mcp_client = get_streamable_http_mcp_client()

# Integrate with Bedrock AgentCore
app = BedrockAgentCoreApp()

# Instantiate model
llm = load_model()

@app.entrypoint
async def invoke(payload):
    # assume payload input is structured as { "prompt": "<user input>" }

    with tracer.start_as_current_span("agentcore-example") as span:
        # Load MCP Tools
        tools = await mcp_client.get_tools()

        # Define the agent
        graph = _create_react_agent(llm, tools=tools + [add_numbers]).with_config(
            {
                "metadata": { "agent_name": "example-agent"}
            }
        )

        # Process the user prompt
        prompt = payload.get("prompt", "What is Agentic AI?")

        # Run the agent
        result = await graph.ainvoke({"messages": [HumanMessage(content=prompt)]})

        # Return result
        return {
            "result": result["messages"][-1].content
        }

if __name__ == "__main__":
    app.run()