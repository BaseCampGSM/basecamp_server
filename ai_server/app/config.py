"""
AWS Bedrock 및 환경변수 설정 모듈
"""

import os
from dotenv import load_dotenv

# .env 파일 로드
load_dotenv(os.path.join(os.path.dirname(os.path.dirname(__file__)), ".env"))


class Settings:
    """애플리케이션 설정"""

    # AWS Bedrock 설정
    AWS_REGION: str = os.getenv("AWS_REGION", "us-east-1")
    AWS_ACCESS_KEY_ID: str = os.getenv("AWS_ACCESS_KEY_ID", "")
    AWS_SECRET_ACCESS_KEY: str = os.getenv("AWS_SECRET_ACCESS_KEY", "")

    # Bedrock 모델 ID
    BEDROCK_EMBEDDING_MODEL_ID: str = os.getenv(
        "BEDROCK_EMBEDDING_MODEL_ID", "amazon.titan-embed-text-v2:0"
    )
    BEDROCK_LLM_MODEL_ID: str = os.getenv(
        "BEDROCK_LLM_MODEL_ID", "anthropic.claude-3-haiku-20240307-v1:0"
    )

    # RAG 설정
    DOCS_DIR: str = os.path.join(os.path.dirname(os.path.dirname(__file__)), "docs")
    CHUNK_SIZE: int = int(os.getenv("CHUNK_SIZE", "500"))
    CHUNK_OVERLAP: int = int(os.getenv("CHUNK_OVERLAP", "50"))
    TOP_K_RESULTS: int = int(os.getenv("TOP_K_RESULTS", "3"))

    # 서버 설정
    HOST: str = os.getenv("HOST", "0.0.0.0")
    PORT: int = int(os.getenv("PORT", "8000"))


settings = Settings()
