import os
from fastapi import APIRouter, UploadFile, File, HTTPException
from app.services.pdf_service import extract_text_from_pdf
from app.services.chroma_service import add_pdf_to_chroma

router = APIRouter(prefix="/upload", tags=["Upload PDF"])

UPLOAD_DIR = "uploaded_pdfs"
os.makedirs(UPLOAD_DIR, exist_ok=True)

@router.post("/pdf")
async def upload_pdf(file: UploadFile = File(...)):
    """
    Accepts a PDF file, extracts its text, chunks it,
    embeds it, and stores it in ChromaDB.
    """
    if not file.filename.lower().endswith(".pdf"):
        raise HTTPException(status_code=400, detail="Only PDF files are allowed")

    # Save PDF temporarily
    file_path = os.path.join(UPLOAD_DIR, file.filename)
    
    with open(file_path, "wb") as f:
        f.write(await file.read())

    # Extract text
    pdf_text = extract_text_from_pdf(file_path)

    # Store in Chroma
    total_chunks = add_pdf_to_chroma(pdf_text)

    return {
        "status": "success",
        "filename": file.filename,
        "chunks_stored": total_chunks
    }
