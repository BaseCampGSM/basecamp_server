from typing import List
from pydantic import BaseModel, Field

class IssueRequest(BaseModel):
    location: str = Field(..., description="위치/행정동")
    user_query: str = Field(..., description="주민 불편사항 텍스트")

class IssueResponse(BaseModel):
    status: str = Field(default="success")
    category: str = Field(..., description="민원 카테고리")
    urgency: str = Field(..., description="긴급도")
    solution: str = Field(..., description="처리 절차 및 해결 가이드")
    sources: List[str] = Field(default=["지자체 표준 행정 민원 가이드"])
