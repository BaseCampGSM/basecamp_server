import json, logging, boto3
from app.config import settings
from app.schemas.query import IssueResponse

logger = logging.getLogger(__name__)

SYSTEM_PROMPT = "당신은 지자체 민원 분석 전문가입니다. 유저의 민원과 위치를 바탕으로 행정 카테고리, 긴급도, 해결방안을 JSON으로 답하세요. category(도로/조명,안전/교통,환경/위생,시설관리,복지/문화,긴급신고,해당없음 중 하나), urgency(상/중/하), solution(3~5문장으로 관할부서 신고방법 예상처리기간 주민행동 포함), sources(항상 지자체 표준 행정 민원 가이드). 민원 아닌 내용은 category를 해당없음으로 하고 solution에 민원 사항이 아닙니다라고 답하세요. 반드시 JSON만 출력하세요."

class AnalysisService:
    def __init__(self):
        self.client = None
        self.llm_ready = False
        self._initialization_error = None

    def initialize(self):
        try:
            self.client = boto3.client("bedrock-runtime", region_name=settings.AWS_REGION)
            self.llm_ready = True
            logger.info("Bedrock 초기화 완료")
            return True
        except Exception as e:
            self._initialization_error = str(e)
            logger.error(f"Bedrock 초기화 실패: {e}")
            return False

    async def analyze_issue(self, location, user_query):
        try:
            if not self.llm_ready:
                return self._fallback(location, user_query)
            body = json.dumps({
                "anthropic_version": "bedrock-2023-05-31",
                "max_tokens": 1024,
                "temperature": 0.1,
                "system": SYSTEM_PROMPT,
                "messages": [{"role": "user", "content": "제보 위치: " + location + "\n민원 내용: " + user_query}]
            })
            response = self.client.invoke_model(
                modelId=settings.BEDROCK_LLM_MODEL_ID,
                body=body,
                contentType="application/json",
                accept="application/json"
            )
            result_body = json.loads(response["body"].read())
            text = result_body["content"][0]["text"].strip()
            if text.startswith("```"):
                text = text.split("\n", 1)[1].rsplit("```", 1)[0].strip()
            result = json.loads(text)
            return IssueResponse(
                status="success",
                category=result.get("category", "기타"),
                urgency=result.get("urgency", "중"),
                solution=result.get("solution", "관할 구청에 문의하세요."),
                sources=["지자체 표준 행정 민원 가이드"]
            )
        except Exception as e:
            logger.error(f"AI 분석 오류: {e}")
            return self._fallback(location, user_query)

    def _fallback(self, location, user_query):
        return IssueResponse(
            status="success",
            category="기타",
            urgency="중",
            solution=location + " 관할 구청에 민원을 접수해 주세요. 120 콜센터를 이용하세요.",
            sources=["지자체 표준 행정 민원 가이드"]
        )

    def get_status(self):
        return {"llm_ready": self.llm_ready, "model": settings.BEDROCK_LLM_MODEL_ID}

analysis_service = AnalysisService()
