"""
Bedrock Claude LLM 민원 분석 서비스
- RAG 없이 순수 LLM만 사용하여 범용 민원 분석 수행
- AWS/Bedrock 연결 실패 시 키워드 기반 Fallback 응답
"""

import logging

from langchain_aws import ChatBedrock
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import JsonOutputParser

from app.config import settings
from app.schemas.query import IssueResponse

logger = logging.getLogger(__name__)

# ──────────────────────────────────────────────────────────────
# 시스템 프롬프트 - 지자체 민원 분석 전문가
# ──────────────────────────────────────────────────────────────
SYSTEM_PROMPT = """당신은 지자체 민원 분석 전문가입니다.
유저의 민원과 위치를 바탕으로 적절한 행정 카테고리, 긴급도, 지자체 차원의 현실적인 해결 방안 및 안내를 작성하세요.

## 분석 기준

1. **category** (민원 카테고리, 문자열 1개):
   다음 중 가장 적합한 하나를 선택:
   - "도로/조명" : 가로등 고장, 도로 파손, 포트홀, 보안등, 보행로
   - "안전/교통" : 교통사고 위험, 신호등 고장, 불법주정차, 보행 안전, CCTV
   - "환경/위생" : 쓰레기, 악취, 소음, 해충, 하수도, 오수
   - "시설관리" : 놀이터, 공원, 벤치, 공공시설 파손/노후
   - "복지/문화" : 복지서비스, 문화시설, 교육 프로그램, 청소년/노인 지원
   - "긴급신고" : 폭행, 화재, 붕괴, 학대 등 즉각 신고 필요한 사안

2. **urgency** (긴급도):
   - "상": 즉각적 안전 위협, 인명 피해 우려 → 112/119 신고 필요
   - "중": 생활 불편 + 잠재적 위험 → 관할 구청 신속 처리 필요
   - "하": 개선 요청, 정보 문의 → 일반 민원 접수

3. **solution** (처리 절차 요약 + 해결 대안 가이드):
   - 해당 민원의 관할 부서 (예: ○○구청 도로과, 도시디자인과 등)
   - 신고/접수 방법 (전화번호, 앱, 웹사이트 등)
   - 예상 처리 기간
   - 주민이 당장 할 수 있는 현실적 행동
   - 3~5문장으로 구체적이고 실용적으로 작성

4. **sources**: 항상 ["지자체 표준 행정 민원 가이드"]로 고정

## 민원 정보
- 제보 위치: {location}
- 민원 내용: {user_query}

## 응답 형식
반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트 없이 JSON만 출력:
{{
    "category": "카테고리",
    "urgency": "상/중/하",
    "solution": "처리 절차 및 해결 가이드",
    "sources": ["지자체 표준 행정 민원 가이드"]
}}
"""


class AnalysisService:
    """Bedrock Claude LLM 민원 분석 서비스"""

    def __init__(self):
        self.llm = None
        self.llm_ready = False
        self._initialization_error: str | None = None

    def initialize(self) -> bool:
        """Bedrock LLM 초기화"""
        try:
            self.llm = ChatBedrock(
                model_id=settings.BEDROCK_LLM_MODEL_ID,
                region_name=settings.AWS_REGION,
                model_kwargs={
                    "max_tokens": 1024,
                    "temperature": 0.1,
                },
            )
            self.llm_ready = True
            logger.info("Bedrock LLM 초기화 완료")
            return True

        except Exception as e:
            self._initialization_error = str(e)
            logger.error(f"Bedrock LLM 초기화 실패: {e}")
            return False

    async def analyze_issue(self, location: str, user_query: str) -> IssueResponse:
        """
        주민 제보를 LLM으로 분석하여 결과를 반환합니다.
        LLM 호출 실패 시 키워드 기반 Fallback 응답을 반환합니다.
        """
        try:
            if not self.llm_ready:
                logger.warning("LLM 미초기화 상태 - Fallback 응답 반환")
                return self._get_fallback_response(location, user_query)

            # LLM 프롬프트 구성 및 호출
            prompt = ChatPromptTemplate.from_messages(
                [("system", SYSTEM_PROMPT)]
            )

            chain = prompt | self.llm | JsonOutputParser()

            result = await chain.ainvoke(
                {
                    "location": location,
                    "user_query": user_query,
                }
            )

            return IssueResponse(
                status="success",
                category=result.get("category", "기타"),
                urgency=result.get("urgency", "중"),
                solution=result.get("solution", "관할 구청에 민원을 접수해 주세요."),
                sources=["지자체 표준 행정 민원 가이드"],
            )

        except Exception as e:
            logger.error(f"AI 분석 중 오류 발생: {e}")
            return self._get_fallback_response(location, user_query)

    def _get_fallback_response(self, location: str, user_query: str) -> IssueResponse:
        """
        Bedrock 연결 실패 시 키워드 기반 Fallback 응답.
        어떤 상황에서도 에러 없이 유효한 JSON 응답을 반환합니다.
        """
        # 긴급 키워드
        emergency_keywords = ["폭행", "화재", "붕괴", "학대", "위협", "사고", "쓰러", "출혈", "감전"]
        high_keywords = ["위험", "고장", "어두", "파손", "침수", "악취", "소음", "꺼져"]

        # 카테고리 매핑
        category_map = {
            "도로/조명": ["가로등", "조명", "보안등", "도로", "포트홀", "보행로"],
            "안전/교통": ["교통", "신호등", "횡단보도", "불법주차", "과속", "CCTV"],
            "환경/위생": ["쓰레기", "악취", "소음", "해충", "하수", "오수"],
            "시설관리": ["놀이터", "공원", "벤치", "시설", "수리"],
            "복지/문화": ["복지", "문화", "도서관", "청소년", "노인", "어르신", "돌봄"],
            "긴급신고": ["폭행", "화재", "붕괴", "학대", "감전"],
        }

        # 긴급도 판단
        is_emergency = any(kw in user_query for kw in emergency_keywords)
        if is_emergency:
            urgency = "상"
        elif any(kw in user_query for kw in high_keywords):
            urgency = "중"
        else:
            urgency = "하"

        # 카테고리 판단
        category = "기타"
        for cat, keywords in category_map.items():
            if any(kw in user_query for kw in keywords):
                category = cat
                break

        # 위치에서 구 이름 추출
        district = "해당 구청"
        for part in location.split():
            if part.endswith("구") or part.endswith("군") or part.endswith("시"):
                district = part
                break

        # 솔루션 생성
        if is_emergency:
            solution = (
                f"긴급 상황입니다. 즉시 112(경찰) 또는 119(소방)에 신고하세요. "
                f"신고 후 {district}청 안전관리과에도 접수하시면 후속 조치가 진행됩니다. "
                f"현장에서 안전한 곳으로 대피 후 신고해 주세요."
            )
        else:
            solution = (
                f"{location} 관할 {district}청에 민원을 접수해 주세요. "
                f"접수 방법: ① {district}청 대표전화, ② 광주시 120 콜센터, "
                f"③ 안전신문고 앱 또는 정부24. "
                f"일반 민원은 접수 후 7일 이내, 긴급 건은 48시간 이내 현장 점검이 진행됩니다. "
                f"(AI 서비스 일시 제한으로 간이 분석 결과입니다)"
            )

        return IssueResponse(
            status="success",
            category=category,
            urgency=urgency,
            solution=solution,
            sources=["지자체 표준 행정 민원 가이드"],
        )

    def get_status(self) -> dict:
        """서비스 상태 정보 반환"""
        return {
            "llm_ready": self.llm_ready,
            "llm_model": settings.BEDROCK_LLM_MODEL_ID,
            "mode": "LLM 직접 분석",
            "initialization_error": self._initialization_error,
        }


# 싱글톤 인스턴스
analysis_service = AnalysisService()
