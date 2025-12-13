import os
from dotenv import load_dotenv
from chromadb import PersistentClient
from openai import OpenAI
from app.services.embedding_service import get_embedding

load_dotenv()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

CHROMA_PATH = "chroma_store"
COLLECTION_NAME = "infino_vault_ai"

def generate_answer(context: str, question: str) -> str:
    """
    Your original RAG answer generator from query_chroma.py.
    Cleaned for API usage.
    """
    prompt = f"""
You are a helpful assistant.
Use only the context provided below to answer clearly and concisely.

Context:
{context}

Question:
{question}

Answer:
""".strip()

    response = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=[{"role": "user", "content": prompt}],
        temperature=0.2
    )
    return response.choices[0].message.content.strip()

def query_rag(question: str):
    """
    Equivalent of your query_chroma.py → query_chroma().
    """
    client_db = PersistentClient(path=CHROMA_PATH)
    collection = client_db.get_or_create_collection(COLLECTION_NAME)

    q_emb = get_embedding(question)
    results = collection.query(query_embeddings=[q_emb], n_results=5)
    docs = (results.get("documents") or [[]])[0]

    if not docs:
        return "No relevant context found."

    context = " ".join(docs)
    answer = generate_answer(context, question)
    return answer
