from pypdf import PdfReader

def extract_text_from_pdf(pdf_path: str) -> str:
    """
    Extract text from any uploaded PDF file.
    Uses your original pdf_loader.py logic.
    """
    reader = PdfReader(pdf_path)
    text = ""
    for page in reader.pages:
        text += page.extract_text() + "\n"
    return text
