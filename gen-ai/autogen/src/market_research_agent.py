import asyncio
from autogen_core import MessageContext, RoutedAgent, message_handler
from autogen_agentchat.agents import AssistantAgent
from autogen_agentchat.messages import TextMessage
from autogen_ext.models.openai import OpenAIChatCompletionClient
from autogen_ext.tools.mcp import StdioServerParams, mcp_server_tools

from message import Message

fetch_mcp_server = StdioServerParams(command="uvx", args=["mcp-server-fetch"], read_timeout_seconds=30)
fetcher = asyncio.run(mcp_server_tools(fetch_mcp_server))

class MarketResearchAgent(RoutedAgent):
    def __init__(self, name: str) -> None:
        super().__init__(name)
        model_client = OpenAIChatCompletionClient(model="gpt-4o-mini")

        self._delegate = AssistantAgent(name, model_client=model_client, tools=fetcher, reflect_on_tool_use=True)

    @message_handler
    async def handle_message(self, message: Message, ctx: MessageContext) -> Message:
        text_message = TextMessage(content=message.content, source="user")
        response = await self._delegate.on_messages([text_message], ctx.cancellation_token)
        return Message(content=response.chat_message.content)