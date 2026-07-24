import os
from dotenv import load_dotenv
load_dotenv(os.path.join(os.path.dirname(os.path.dirname(__file__)), ".env"))

class Settings:
    AWS_REGION: str = os.getenv("AWS_REGION", "eu-central-1")
    BEDROCK_LLM_MODEL_ID: str = os.getenv("BEDROCK_LLM_MODEL_ID", "anthropic.claude-3-haiku-20240307-v1:0")
    HOST: str = os.getenv("HOST", "0.0.0.0")
    PORT: int = int(os.getenv("PORT", "8001"))

settings = Settings()
