import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.schemas.query import IssueRequest, IssueResponse
from app.services.rag_service import analysis_service

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("AI 서버 시작")
    analysis_service.initialize()
    yield

app = FastAPI(title="TOPIC 12 AI Server", version="4.0.0", lifespan=lifespan)
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_credentials=False, allow_methods=["*"], allow_headers=["*"])

@app.get("/api/status")
async def get_status():
    return {"server": "running", "ai": analysis_service.get_status()}

@app.post("/api/ai/solve-issue", response_model=IssueResponse)
async def solve_issue(request: IssueRequest):
    logger.info(f"제보 접수 - 위치: {request.location}")
    return await analysis_service.analyze_issue(request.location, request.user_query)
