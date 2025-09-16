import asyncio
from autogen_core import AgentId
from autogen_ext.runtimes.grpc import GrpcWorkerAgentRuntime
from autogen_ext.runtimes.grpc import GrpcWorkerAgentRuntimeHost
from idea_generator_agent import IdeaGeneratorAgent
from market_research_agent import MarketResearchAgent
from evaluator_agent import EvaluatorAgent
from message import Message
from opentelemetry import trace
import openlit

tracer = trace.get_tracer("autogen-example")

async def main():

    openlit.init(environment="test")

    host = GrpcWorkerAgentRuntimeHost(address="localhost:50052")
    host.start()

    worker1 = GrpcWorkerAgentRuntime(host_address="localhost:50052")
    await worker1.start()
    await IdeaGeneratorAgent.register(worker1, "idea_generator_agent", lambda: IdeaGeneratorAgent("idea_generator_agent"))

    worker2 = GrpcWorkerAgentRuntime(host_address="localhost:50052")
    await worker2.start()
    await MarketResearchAgent.register(worker2, "market_research_agent", lambda: MarketResearchAgent("market_research_agent"))

    worker = GrpcWorkerAgentRuntime(host_address="localhost:50052")
    await worker.start()
    await EvaluatorAgent.register(worker, "evaluator_agent", lambda: EvaluatorAgent("evaluator_agent"))
    agent_id = AgentId("evaluator_agent", "default")

    # start a span manually, so we capture all of the evaluation steps in a single trace
    with tracer.start_as_current_span("invoke-workflow") as current_span:
        response = await worker.send_message(Message(content="Go!"), agent_id)

    print(response.content)

    await worker.stop()
    await worker1.stop()
    await worker2.stop()
    await host.stop()

if __name__ == "__main__":
    asyncio.run(main())
