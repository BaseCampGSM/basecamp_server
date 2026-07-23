"""
Pydantic Request/Response 모델 정의
- 주민 제보 분석 요청 및 AI 분석 결과 응답 스키마
"""

from typing import List
from pydantic import BaseModel, Field


class IssueRequest(BaseModel):
    """주민 제보 분석 요청 모델"""

    location: str = Field(
        ...,
        description="위치/행정동 (예: 광주 북구 운암동)",
        examples=["광주 북구 운암동"],
    )
    user_query: str = Field(
        ...,
        description="주민 불편사항 텍스트",
        examples=["우리 동네 놀이터 조명이 고장나서 밤에 너무 어두워요. 아이들이 위험해요."],
    )


class IssueResponse(BaseModel):
    """주민 제보 AI 분석 결과 응답 모델"""

    status: str = Field(
        default="success",
        description="처리 상태 (success / error)",
    )
    category: List[str] = Field(
        ...,
        description="다중 태깅/카테고리 (예: ['안전', '시설관리'])",
    )
    priority: str = Field(
        ...,
        description="긴급도 (상 / 중 / 하)",
    )
    summary: str = Field(
        ...,
        description="AI 분석 핵심 요약",
    )
    recommended_actions: List[str] = Field(
        ...,
        description="실천/안내 절차 리스트",
    )
    is_emergency: bool = Field(
        ...,
        description="긴급신고 필요 여부 (True: 즉시 신고 필요)",
    )
