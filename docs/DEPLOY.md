# 배포 & CI/CD 가이드 (AWS EC2 + Docker + GitHub Actions + GHCR)

`basecamp_server`(Spring Boot) 를 **AWS EC2**에 **Docker** 컨테이너로 배포하고 **GitHub Actions**로 자동화한다.

> ℹ️ 이 프로젝트의 AWS 계정은 **교육용 제한 계정**이라 **ECR·IAM 을 만들 수 없다.**
> 그래서 이미지 저장소는 AWS 밖 → **GitHub 자체 저장소 GHCR(ghcr.io)** 을 쓴다.
> **AWS 에서 필요한 건 EC2 인스턴스 하나뿐**이고, AWS 액세스 키도 필요 없다.

## 전체 그림

```
개발자 → git push (develop)
            │
            ▼
   GitHub Actions (.github/workflows/deploy.yml)
     ├─ test  : JDK 21 로 ./gradlew build (컴파일 + 테스트)
     └─ deploy:
          1) Docker 이미지 빌드
          2) GHCR(ghcr.io) 에 push        ← GITHUB_TOKEN 사용(별도 키 X)
          3) EC2 에 SSH → docker compose pull & up
            │
            ▼
        AWS EC2 (Ubuntu, Docker)  →  http://<EC2 퍼블릭 IP>:8080
        (EC2 는 GHCR 에서 이미지 pull)
```

- 빌드는 GitHub Actions, **EC2 는 완성 이미지 실행만** → 1GB 프리티어도 OK.
- DB 는 **Supabase(관리형 PostgreSQL)** 를 쓴다. EC2 에 별도 DB 컨테이너를 띄우지 않는다.
- AI 분석은 Spring 백엔드가 **Claude API를 직접 호출**한다(팀원의 별도 FastAPI 서비스를 거치지 않음).

---

## Phase 1 — EC2 인스턴스 생성 (AWS 콘솔)

> 리전은 이 계정에서 쓸 수 있는 **us-east-2 (Ohio)** 로 진행 (콘솔 우측 상단 리전 확인).

**EC2 → Instances → Launch instances**
1. Name: `basecamp-server`
2. AMI: **Ubuntu Server 24.04 LTS**
3. Instance type: **t3.micro** (프리티어; 막혀 있으면 t2.micro 시도)
4. **Key pair**: *Create new key pair* → 이름 `basecamp-key`, RSA, `.pem` → 다운로드
   - (키페어 생성이 막혀 있으면: 로컬에서 `ssh-keygen` 후 *Import key pair* 로 공개키 등록)
5. **Network settings → 보안그룹** 인바운드 규칙:
   | 타입 | 포트 | 소스 | 용도 |
   |---|---|---|---|
   | SSH | 22 | `0.0.0.0/0` | Actions 가 SSH 로 배포 (키 인증만 허용) |
   | Custom TCP | 8080 | `0.0.0.0/0` | 앱 접속 |
6. Launch → 생성 후 **퍼블릭 IPv4 주소** 확인 (= 시크릿 `EC2_HOST`)

## Phase 2 — EC2 초기 세팅 (한 번만)

로컬에서 접속:
```bash
chmod 400 basecamp-key.pem
ssh -i basecamp-key.pem ubuntu@<EC2 퍼블릭 IP>
```
EC2 안에서:
```bash
# Docker + compose 플러그인
sudo apt-get update
sudo apt-get install -y docker.io docker-compose-v2
sudo usermod -aG docker ubuntu

# 1GB 램 대비 swap 2GB (컨테이너 OOM 방지)
sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile
sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# 배포 디렉토리
mkdir -p /home/ubuntu/basecamp

exit   # docker 그룹 적용 위해 재접속
```
> AWS CLI 는 설치할 필요 없다(ECR 안 씀). EC2 의 GHCR 로그인은 Actions 가 배포 시 임시 토큰으로 처리한다.

## Phase 2.5 — EC2에 실제 시크릿(.env) 만들기 (⚠️ 본인이 직접, 한 번만)

앱이 실제로 필요로 하는 자격증명(Supabase DB, Google OAuth, Claude API, 공공데이터/카카오 키)은
**git 이나 GitHub Secrets 가 아니라 EC2 안의 `.env` 파일**에만 존재해야 한다.
(누가 대신 만들어줄 수 없는 단계 — 실제 비밀번호/키가 들어가기 때문)

```bash
ssh -i basecamp-key.pem ubuntu@<EC2 퍼블릭 IP>
cd /home/ubuntu/basecamp
nano .env
```
저장소의 [.env.example](../.env.example) 에 있는 키 이름 그대로, **실제 값**을 채워 저장(`Ctrl+O`, `Ctrl+X`):
```
SUPABASE_DB_HOST=...
SUPABASE_DB_USER=...
SUPABASE_DB_PASSWORD=...
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
ANTHROPIC_API_KEY=...
PUBLIC_DATA_API_KEY=...
LOCAL_DATA_API_KEY=...
KAKAO_REST_API_KEY=...
```
값들은 Supabase 콘솔(Project Settings → Database), Google Cloud Console(OAuth 클라이언트), Anthropic 콘솔, 공공데이터포털/카카오 디벨로퍼스에서 각각 발급받은 실제 값. 이 값들을 아는 팀원(백엔드/AI 담당)에게 전달받아 채운다.

## Phase 3 — GitHub 저장소 시크릿 등록 (3개뿐)

**저장소 → Settings → Secrets and variables → Actions → New repository secret**

| 시크릿 | 값 |
|---|---|
| `EC2_HOST` | EC2 퍼블릭 IP |
| `EC2_USER` | `ubuntu` |
| `EC2_SSH_KEY` | `basecamp-key.pem` **파일 내용 전체** (`-----BEGIN...END-----` 포함) |

> AWS 액세스 키·리전·ECR 관련 시크릿은 **필요 없다.** 이미지 경로는 `deploy.yml`의 `IMAGE_NAME` 에 고정돼 있다.

## Phase 4 — 배포 실행 & 확인

```bash
git push origin develop
```
- 저장소 **Actions 탭** → `CI/CD` 워크플로 로그 (test → deploy 순서)
- 첫 배포 후 **GHCR 패키지**가 저장소 우측 *Packages* 에 생성됨(비공개, 정상)
- 브라우저에서 **`http://<EC2 퍼블릭 IP>:8080`** 접속
- Google 로그인 화면/리다이렉트가 뜨면 정상 기동. `/api/v1/...` 호출 시 CORS 는 `http://localhost:3000` 프론트 기준으로 이미 설정돼 있음.

이후 팀원이 `develop`에 머지할 때마다 자동 배포된다.

---

## CI 테스트는 실제 DB 없이 돈다 (H2)

`application.yml`은 Supabase(Postgres) 접속 정보를 요구하는데, GitHub Actions 러너에는 그 값이 없다.
그래서 [src/test/resources/application.yml](../src/test/resources/application.yml) 로 **테스트 실행 시에만** H2 인메모리 DB + 더미 값으로 오버라이드해 컨텍스트가 뜨도록 했다.
실제 프로덕션 설정(`src/main/resources/application.yml`)은 건드리지 않으며, Supabase 자격증명을 CI 에 넣을 필요가 없다.

## 트러블슈팅

- **EC2 에서 이미지 pull 실패(unauthorized)**: 배포 스크립트의 GHCR 로그인 단계 확인. 패키지가 조직 소속이라 접근 안 되면, 저장소 *Packages → Package settings* 에서 해당 repo 에 권한 부여 또는 **가시성 Public** 으로 변경.
- **GHCR 이름 오류**: 이미지 경로는 **소문자만** 허용 (`ghcr.io/basecampgsm/basecamp_server`).
- **SSH timeout**: 보안그룹 22 인바운드, `EC2_HOST`, `EC2_SSH_KEY`(전체 내용) 확인.
- **컨테이너 OOM**: swap 설정 확인. 힙은 Dockerfile `JAVA_OPTS`(MaxRAMPercentage)로 제한 중.
- **컨테이너가 뜨자마자 죽음(Supabase 연결 실패 등)**: EC2 의 `.env` 값 확인(Phase 2.5). `docker logs basecamp-server` 로 원인 확인.
- **test 잡 DataSource 실패**: `src/test/resources/application.yml` 이 존재하는지, H2 의존성(`build.gradle`의 `com.h2database:h2`)이 유지되는지 확인.

## 참고 파일
- 파이프라인: [.github/workflows/deploy.yml](../.github/workflows/deploy.yml)
- 이미지 빌드: [Dockerfile](../Dockerfile)
- 실행 정의: [docker-compose.yml](../docker-compose.yml)
- EC2 `.env` 템플릿(이름만, 값 없음): [.env.example](../.env.example)
- 테스트 전용 설정(H2): [src/test/resources/application.yml](../src/test/resources/application.yml)
