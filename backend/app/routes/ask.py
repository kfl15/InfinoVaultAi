from fastapi import APIRouter
from pydantic import BaseModel
from app.services.rag_service import query_rag

router = APIRouter(prefix="/ask", tags=["Ask Questions"])

class AskRequest(BaseModel):
    question: str

@router.post("/")
async def ask_question(body: AskRequest):
    """
    Accepts a user question, queries ChromaDB,
    and returns an AI-generated answer.
    """
    answer = query_rag(body.question)
    return {
        "question": body.question,
        "answer": answer
    }
