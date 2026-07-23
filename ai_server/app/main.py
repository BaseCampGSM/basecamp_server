"""
FastAPI 메인 애플리케이션
- GET  /api/status         : AI 서버 상태 체크
- POST /api/ai/solve-issue : 주민 제보 분석 (Bedrock Claude LLM 직접 분석)
"""

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.schemas.query import IssueRequest, IssueResponse
from app.services.rag_service import analysis_service

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """애플리케이션 시작/종료 이벤트 핸들러"""
    logger.info("AI 서버 시작 - Bedrock LLM 초기화 중...")
    initialized = analysis_service.initialize()
    if initialized:
        logger.info("Bedrock LLM 초기화 성공 - LLM 직접 분석 모드")
    else:
        logger.warning("Bedrock LLM 초기화 실패 - 키워드 Fallback 모드로 운영됩니다")
    yield
    logger.info("AI 서버 종료")


app = FastAPI(
    title="TOPIC 12: 지역 문제 해결 정보 서비스 - AI Server",
    description="주민 제보 기반 AI 분석 서비스 (Bedrock Claude LLM 직접 분석)",
    version="3.0.0",
    lifespan=lifespan,
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/api/status")
async def get_status():
    """AI 서버 상태 체크"""
    service_status = analysis_service.get_status()
    return {
        "server": "running",
        "service": "TOPIC 12 - 지역 문제 해결 AI 서비스",
        "ai": service_status,
    }


@app.post("/api/ai/solve-issue", response_model=IssueResponse)
async def solve_issue(request: IssueRequest):
    """
    주민 제보 분석 엔드포인트

    - 어떤 지역, 어떤 민원이든 Bedrock Claude가 직접 분석
    - RAG/벡터DB 의존 없음

    응답: { status, category, urgency, solution, sources }
    """
    logger.info(f"제보 접수 - 위치: {request.location}, 내용: {request.user_query[:50]}...")

    result = await analysis_service.analyze_issue(
        location=request.location,
        user_query=request.user_query,
    )

    logger.info(
        f"분석 완료 - 카테고리: {result.category}, 긴급도: {result.urgency}"
    )
    return result
