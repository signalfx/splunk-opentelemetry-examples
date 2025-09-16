from autogen_core import AgentId, MessageContext, RoutedAgent, message_handler
from autogen_agentchat.agents import AssistantAgent
from autogen_agentchat.messages import TextMessage
from autogen_ext.models.openai import OpenAIChatCompletionClient
from message import Message

IDEA_GENERATOR_INSTRUCTIONS = "Your role is to generate an idea for a startup based on a market trend or theme"

MARKET_RESEARCH_INSTRUCTIONS = """You will be provided with an idea for a startup.
Search the web using tools if needed to see which competitors already exist, if any.
Estimate the Total Addressable Market (TAM) for this idea."""

EVALUATION_INSTRUCTIONS = """You must make a decision on whether to proceed with the proposed startup idea.
Based purely on the research from your market analyst, please respond with your decision and brief rationale."""

class EvaluatorAgent(RoutedAgent):

    def __init__(self, name: str) -> None:
        super().__init__(name)
        model_client = OpenAIChatCompletionClient(model="gpt-4o-mini")
        self._delegate = AssistantAgent(name, model_client=model_client)

    @message_handler
    async def handle_message(self, message: Message, ctx: MessageContext) -> Message:
        idea_generator_message = Message(content=IDEA_GENERATOR_INSTRUCTIONS)
        market_research_message = Message(content=MARKET_RESEARCH_INSTRUCTIONS)
        idea_generator_agent = AgentId("idea_generator_agent", "default")
        market_research_agent = AgentId("market_research_agent", "default")
        idea = await self.send_message(idea_generator_message, idea_generator_agent)

        market_research_request = f"## Startup Idea:\n{idea.content}\n\n{MARKET_RESEARCH_INSTRUCTIONS}\n\n"
        market_research = await self.send_message(Message(content=market_research_request), market_research_agent)

        result = f"## Startup Idea:\n{idea.content}\n\n## Market Research Result:\n{market_research.content}\n\n"
        evaluation = f"{EVALUATION_INSTRUCTIONS}\n{result}Respond with your decision and brief explanation"
        message = TextMessage(content=evaluation, source="user")
        response = await self._delegate.on_messages([message], ctx.cancellation_token)
        return Message(content=result + "\n\n## Decision:\n\n" + response.chat_message.content)