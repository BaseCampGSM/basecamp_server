"""
RAG 파이프라인 서비스 모듈
- FAISS 벡터 DB + Bedrock Embeddings + Claude LLM
- 긴급도 판단, 카테고리 다중 분류, 출처 근거 기반 답변 요약
- AWS/Bedrock 연결 실패 시 Fallback 응답
"""

import os
import logging
from typing import Optional

from langchain_aws import BedrockEmbeddings, ChatBedrock
from langchain_community.vectorstores import FAISS
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import JsonOutputParser

from app.config import settings
from app.schemas.query import IssueResponse

logger = logging.getLogger(__name__)

# 시스템 프롬프트 - 주민 제보 분석 전문가
SYSTEM_PROMPT = """당신은 광주 북구 지역 문제 해결 AI 분석 전문가입니다.
주민이 제보한 불편사항을 분석하여 다음 항목을 JSON 형식으로 반환해야 합니다.

분석 기준:
1. **긴급도 판단 (priority)**: 
   - "상": 즉각적인 안전 위협, 인명 피해 우려 (예: 화재, 폭행, 붕괴 위험, 아동학대)
   - "중": 일상생활 불편 및 잠재적 위험 (예: 가로등 고장, 도로 파손, 시설 노후)
   - "하": 개선 요청, 정보 문의, 일반 민원 (예: 프로그램 문의, 시설 이용 안내)

2. **긴급신고 필요 여부 (is_emergency)**:
   - true: 즉각적인 신고가 필요한 경우 (112, 119, 1388 등)
   - false: 일반 민원 처리 가능

3. **카테고리 다중 분류 (category)**:
   가능한 카테고리: 안전, 시설관리, 도로/교통, 복지, 청소년, 교육/문화, 환경, 긴급신고, 노인복지, 장애인복지

4. **핵심 요약 (summary)**: 제보 내용과 관련 공공정보를 종합하여 2~3문장으로 요약

5. **실천/안내 절차 (recommended_actions)**: 주민이 취할 수 있는 구체적 행동 리스트 (연락처 포함)

참고할 공공 데이터:
{context}

주민 제보 위치: {location}
주민 제보 내용: {user_query}

반드시 아래 JSON 형식으로만 응답하세요:
{{
    "category": ["카테고리1", "카테고리2"],
    "priority": "상/중/하",
    "summary": "분석 요약",
    "recommended_actions": ["행동1", "행동2"],
    "is_emergency": true/false
}}
"""


class RAGService:
    """RAG 파이프라인 서비스 클래스"""

    def __init__(self):
        self.vector_store: Optional[FAISS] = None
        self.embeddings = None
        self.llm = None
        self.is_initialized = False
        self._initialization_error: Optional[str] = None

    def initialize(self) -> bool:
        """RAG 파이프라인 초기화 (Embeddings, FAISS, LLM)"""
        try:
            # Bedrock Embeddings 초기화
            self.embeddings = BedrockEmbeddings(
                model_id=settings.BEDROCK_EMBEDDING_MODEL_ID,
                region_name=settings.AWS_REGION,
            )

            # Bedrock LLM 초기화
            self.llm = ChatBedrock(
                model_id=settings.BEDROCK_LLM_MODEL_ID,
                region_name=settings.AWS_REGION,
                model_kwargs={
                    "max_tokens": 1024,
                    "temperature": 0.1,
                },
            )

            # 문서 로드 및 FAISS 벡터 DB 구축
            self._build_vector_store()

            self.is_initialized = True
            logger.info("RAG 서비스 초기화 완료")
            return True

        except Exception as e:
            self._initialization_error = str(e)
            logger.error(f"RAG 서비스 초기화 실패: {e}")
            return False

    def _build_vector_store(self):
        """docs/ 디렉토리의 텍스트 파일을 로드하고 FAISS 벡터 DB 구축"""
        documents = []

        # docs 디렉토리에서 텍스트 파일 읽기
        docs_dir = settings.DOCS_DIR
        if not os.path.exists(docs_dir):
            raise FileNotFoundError(f"문서 디렉토리를 찾을 수 없습니다: {docs_dir}")

        for filename in os.listdir(docs_dir):
            if filename.endswith(".txt"):
                filepath = os.path.join(docs_dir, filename)
                with open(filepath, "r", encoding="utf-8") as f:
                    content = f.read()
                    documents.append(content)

        if not documents:
            raise ValueError("로드할 문서가 없습니다.")

        # 텍스트 청크 분할
        text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=settings.CHUNK_SIZE,
            chunk_overlap=settings.CHUNK_OVERLAP,
            separators=["\n\n", "\n", ".", " "],
        )

        chunks = []
        for doc in documents:
            chunks.extend(text_splitter.split_text(doc))

        # FAISS 벡터 스토어 생성
        self.vector_store = FAISS.from_texts(
            texts=chunks,
            embedding=self.embeddings,
        )
        logger.info(f"FAISS 벡터 DB 구축 완료: {len(chunks)}개 청크")

    async def analyze_issue(self, location: str, user_query: str) -> IssueResponse:
        """
        주민 제보를 분석하여 결과를 반환합니다.
        AWS/Bedrock 연결 실패 시 Fallback 응답을 반환합니다.
        """
        try:
            if not self.is_initialized:
                logger.warning("RAG 서비스 미초기화 상태 - Fallback 응답 반환")
                return self._get_fallback_response(location, user_query)

            # FAISS에서 관련 문서 검색
            relevant_docs = self.vector_store.similarity_search(
                user_query, k=settings.TOP_K_RESULTS
            )
            context = "\n\n".join([doc.page_content for doc in relevant_docs])

            # LLM 프롬프트 구성
            prompt = ChatPromptTemplate.from_messages(
                [("system", SYSTEM_PROMPT)]
            )

            # LLM 체인 실행
            chain = prompt | self.llm | JsonOutputParser()

            result = await chain.ainvoke(
                {
                    "context": context,
                    "location": location,
                    "user_query": user_query,
                }
            )

            return IssueResponse(
                status="success",
                category=result.get("category", ["기타"]),
                priority=result.get("priority", "중"),
                summary=result.get("summary", "분석 결과를 생성할 수 없습니다."),
                recommended_actions=result.get("recommended_actions", []),
                is_emergency=result.get("is_emergency", False),
            )

        except Exception as e:
            logger.error(f"AI 분석 중 오류 발생: {e}")
            return self._get_fallback_response(location, user_query)

    def _get_fallback_response(self, location: str, user_query: str) -> IssueResponse:
        """
        AWS/Bedrock 연결 실패 시 기획안 예시 데이터 형태로 반환하는 Fallback 응답
        키워드 기반으로 간단한 분류를 수행합니다.
        """
        # 키워드 기반 긴급도 및 카테고리 판단
        emergency_keywords = ["폭행", "화재", "붕괴", "학대", "위협", "사고", "쓰러", "출혈", "감전"]
        high_priority_keywords = ["위험", "고장", "어두", "파손", "침수", "악취", "소음"]
        safety_keywords = ["안전", "위험", "사고", "조명", "가로등", "CCTV"]
        facility_keywords = ["시설", "놀이터", "공원", "도서관", "수리", "고장"]
        road_keywords = ["도로", "포트홀", "보행", "교통", "신호등"]
        welfare_keywords = ["복지", "지원", "돌봄", "상담"]
        youth_keywords = ["청소년", "아이", "학생", "방과후"]
        elderly_keywords = ["노인", "어르신", "독거"]

        # 긴급도 판단
        is_emergency = any(kw in user_query for kw in emergency_keywords)
        if is_emergency:
            priority = "상"
        elif any(kw in user_query for kw in high_priority_keywords):
            priority = "중"
        else:
            priority = "하"

        # 카테고리 분류
        categories = []
        if any(kw in user_query for kw in safety_keywords):
            categories.append("안전")
        if any(kw in user_query for kw in facility_keywords):
            categories.append("시설관리")
        if any(kw in user_query for kw in road_keywords):
            categories.append("도로/교통")
        if any(kw in user_query for kw in welfare_keywords):
            categories.append("복지")
        if any(kw in user_query for kw in youth_keywords):
            categories.append("청소년")
        if any(kw in user_query for kw in elderly_keywords):
            categories.append("노인복지")
        if is_emergency:
            categories.append("긴급신고")
        if not categories:
            categories.append("기타")

        # Fallback 안내 액션
        recommended_actions = [
            f"북구청 민원 접수: 062-410-8119 (안전신고 핫라인)",
            f"광주시 120 콜센터로 문의",
            f"스마트 북구 앱을 통한 민원 신고",
        ]
        if is_emergency:
            recommended_actions.insert(0, "긴급 상황 시 112(경찰) 또는 119(소방)에 즉시 신고")

        return IssueResponse(
            status="success",
            category=categories,
            priority=priority,
            summary=f"[{location}] 지역에서 접수된 민원입니다. "
            f"'{user_query[:50]}...' 내용을 분석한 결과, "
            f"해당 건은 {'/'.join(categories)} 관련 사안으로 분류되었습니다. "
            f"(AI 서비스 연결 제한으로 간이 분석 결과입니다)",
            recommended_actions=recommended_actions,
            is_emergency=is_emergency,
        )

    def get_status(self) -> dict:
        """RAG 서비스 상태 정보 반환"""
        return {
            "rag_initialized": self.is_initialized,
            "vector_store_ready": self.vector_store is not None,
            "embedding_model": settings.BEDROCK_EMBEDDING_MODEL_ID,
            "llm_model": settings.BEDROCK_LLM_MODEL_ID,
            "initialization_error": self._initialization_error,
        }


# 싱글톤 인스턴스
rag_service = RAGService()
