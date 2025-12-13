from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routes.upload_pdf import router as upload_router
from app.routes.ask import router as ask_router

app = FastAPI(title="InfinoVaultAi Backend")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health")
async def health_check():
    return {"status": "ok"}

# Include routers
app.include_router(upload_router)
app.include_router(ask_router)
