# AI 서버(FastAPI + AWS Bedrock) 컨테이너 이미지.
# ⚠️ AWS 자격증명 등 비밀값은 이미지에 넣지 않는다.
#    실행 시 docker compose 의 env_file(.env)로 주입되고, config.py 의 os.getenv 가 읽는다.
FROM python:3.11-slim
WORKDIR /app

# 의존성 먼저 설치(레이어 캐시)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 앱 소스만 복사 (.env 는 .dockerignore 로 제외 → 이미지에 시크릿 미포함)
COPY app ./app

EXPOSE 8001
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]
