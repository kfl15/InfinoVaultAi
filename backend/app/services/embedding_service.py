import os
from dotenv import load_dotenv
from openai import OpenAI

# Absolute path to backend/.env
BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
ENV_PATH = os.path.join(BASE_DIR, ".env")

print("Loading .env from:", ENV_PATH)  # Debug line (optional)

load_dotenv(ENV_PATH)

api_key = os.getenv("OPENAI_API_KEY")

if not api_key:
    raise ValueError(f"OPENAI_API_KEY not found. Make sure it exists in {ENV_PATH}")

client = OpenAI(api_key=api_key)

def get_embedding(text: str):
    """
    Generates an OpenAI embedding for the given text.
    """
    response = client.embeddings.create(
        model="text-embedding-3-small",
        input=text
    )
    return response.data[0].embedding
