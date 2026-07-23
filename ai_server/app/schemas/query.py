"""
Pydantic Request/Response 모델 정의
- 주민 제보 분석 요청 및 AI 분석 결과 응답 스키마
- 프론트엔드 API 스펙: category, urgency, solution, sources
"""

from typing import List
from pydantic import BaseModel, Field


class IssueRequest(BaseModel):
    """주민 제보 분석 요청 모델"""

    location: str = Field(
        ...,
        description="위치/행정동 또는 좌표 (예: 광주 광산구 수완동)",
        examples=["광주 광산구 수완동", "광주 북구 운암동"],
    )
    user_query: str = Field(
        ...,
        description="주민 불편사항/민원 텍스트",
        examples=["우리 동네 가로등이 3일째 꺼져 있어서 밤에 너무 어둡고 위험합니다."],
    )


class IssueResponse(BaseModel):
    """주민 제보 AI 분석 결과 응답 모델 (프론트엔드 연동 스펙)"""

    status: str = Field(
        default="success",
        description="처리 상태 (success / error)",
    )
    category: str = Field(
        ...,
        description="민원 카테고리 (예: 도로/조명, 안전/교통, 환경/위생, 복지/문화, 시설관리, 긴급신고)",
    )
    urgency: str = Field(
        ...,
        description="긴급도 (상 / 중 / 하)",
    )
    solution: str = Field(
        ...,
        description="해당 민원에 대한 지자체 차원의 처리 절차 요약 및 현실적인 해결 대안 가이드",
    )
    sources: List[str] = Field(
        ...,
        description="참고 출처 리스트. RAG 지식베이스 매칭 시 해당 출처, 없으면 ['지자체 표준 민원 가이드']",
    )
