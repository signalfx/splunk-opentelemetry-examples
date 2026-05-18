import os
from azure.identity import DefaultAzureCredential
from azure.ai.projects import AIProjectClient

endpoint = os.getenv("AZURE_FOUNDRY_PROJECT_ENDPOINT")

project_client = AIProjectClient(
    endpoint=endpoint,
    credential=DefaultAzureCredential(),
)

my_agent = "demo-agent"
my_version = "2"

openai_client = project_client.get_openai_client()

# Reference the agent to get a response
response = openai_client.responses.create(
    input=[{"role": "user", "content": "Tell me what you can help with."}],
    extra_body={"agent_reference": {"name": my_agent, "version": my_version, "type": "agent_reference"}},
)

print(f"Response output: {response.output_text}")

