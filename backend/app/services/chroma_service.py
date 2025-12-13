import os
from chromadb import PersistentClient
from app.services.embedding_service import get_embedding

CHROMA_PATH = "chroma_store"
COLLECTION_NAME = "infino_vault_ai"

def chunk_text(text: str, chunk_size=1000, overlap=200):
    """
    Your original chunking logic from populate_chroma.py.
    """
    chunks, start = [], 0
    while start < len(text):
        end = min(start + chunk_size, len(text))
        chunks.append(text[start:end])
        start += chunk_size - overlap
    return chunks

def add_pdf_to_chroma(pdf_text: str):
    """
    Equivalent of your populate_chroma.py but adjusted for FastAPI.
    Stores embeddings chunk-by-chunk.
    """

    os.makedirs(CHROMA_PATH, exist_ok=True)
    client = PersistentClient(path=CHROMA_PATH)
    collection = client.get_or_create_collection(COLLECTION_NAME)

    chunks = chunk_text(pdf_text)
    print(f"Total chunks: {len(chunks)}")

    for i, chunk in enumerate(chunks):
        emb = get_embedding(chunk)
        collection.add(
            ids=[f"chunk_{i}"],
            documents=[chunk],
            embeddings=[emb]
        )

    return len(chunks)
