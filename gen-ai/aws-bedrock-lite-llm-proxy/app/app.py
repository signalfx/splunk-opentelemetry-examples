import os
import openai

# set base_url to LiteLLM proxy
client = openai.OpenAI(api_key='anything',base_url="http://0.0.0.0:4000")

# request sent to model set on litellm proxy, `litellm --model`
response = client.chat.completions.create(model='anything', messages = [
    {
        "role": "user",
        "content": "this is a test request, write a short poem"
    }
])

print(response)