import os
import openai

# set base_url to LiteLLM proxy
client = openai.OpenAI(api_key=os.environ['LITELLM_VIRTUAL_KEY'],base_url="http://0.0.0.0:4000")

# request sent to model set on litellm proxy, `litellm --model`
response = client.chat.completions.create(model="gpt-3.5-turbo", messages = [
    {
        "role": "user",
        "content": "this is a test request, write a short poem"
    }
])

print(response)