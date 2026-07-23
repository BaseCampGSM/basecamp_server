"""
FastAPI 메인 애플리케이션
- GET  /api/status         : AI 서버 및 RAG 상태 체크
- POST /api/ai/solve-issue : 주민 제보 분석 및 RAG 매칭 결과 반환
"""

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.schemas.query import IssueRequest, IssueResponse
from app.services.rag_service import rag_service

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """애플리케이션 시작/종료 이벤트 핸들러"""
    # 시작 시 RAG 서비스 초기화
    logger.info("AI 서버 시작 - RAG 서비스 초기화 중...")
    initialized = rag_service.initialize()
    if initialized:
        logger.info("RAG 서비스 초기화 성공")
    else:
        logger.warning("RAG 서비스 초기화 실패 - Fallback 모드로 운영됩니다")
    yield
    # 종료 시 정리
    logger.info("AI 서버 종료")


app = FastAPI(
    title="TOPIC 12: 지역 문제 해결 정보 서비스 - AI Server",
    description="주민 제보 기반 AI 분석 & RAG 공공정보 매칭 서비스",
    version="1.0.0",
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
    """AI 서버 및 RAG 상태 체크 엔드포인트"""
    rag_status = rag_service.get_status()
    return {
        "server": "running",
        "service": "TOPIC 12 - 지역 문제 해결 AI 서비스",
        "rag": rag_status,
    }


@app.post("/api/ai/solve-issue", response_model=IssueResponse)
async def solve_issue(request: IssueRequest):
    """
    주민 제보 분석 및 RAG 매칭 결과 반환 엔드포인트

    - 주민 불편사항 텍스트를 AI 분석
    - 긴급도 판단 (상/중/하)
    - 카테고리 다중 분류
    - 공공데이터 기반 안내 절차 제공
    - 긴급신고 필요 여부 판별
    """
    logger.info(f"제보 접수 - 위치: {request.location}, 내용: {request.user_query[:50]}...")

    result = await rag_service.analyze_issue(
        location=request.location,
        user_query=request.user_query,
    )

    logger.info(
        f"분석 완료 - 긴급도: {result.priority}, "
        f"카테고리: {result.category}, "
        f"긴급신고: {result.is_emergency}"
    )
    return result
