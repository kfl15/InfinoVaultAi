from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(title="InfinoVaultAi Backend")

# -------------------------------
# CORS SETTINGS
# -------------------------------
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# -------------------------------
# HEALTH CHECK
# -------------------------------
@app.get("/health")
async def health_check():
    return {"status": "ok"}

# -------------------------------
# ROUTER PLACEHOLDERS
# (Will be added in Step 4)
# -------------------------------
# from app.routes.upload_pdf import router as upload_router
# from app.routes.ask import router as ask_router
# app.include_router(upload_router)
# app.include_router(ask_router)
