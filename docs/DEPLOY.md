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
- 현재 `develop`은 빈 스켈레톤이지만, 이 절차로 파이프라인·인프라를 먼저 완성해두면 이후 실제 코드 push 시 자동 배포된다.

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
- 현재는 빈 스켈레톤 + Spring Security 라 **401/로그인 화면이 뜨면 정상 기동**

이후 팀원이 `develop`에 머지할 때마다 자동 배포된다.

---

## 2단계: 실제 코드 + MySQL 연결 (진짜 백엔드 코드가 오면)

1. `application.properties`의 `spring.autoconfigure.exclude=...` **삭제** (DB 자동설정 다시 켬)
2. **MySQL 준비**: `docker-compose.yml`의 `mysql` 블록 주석 해제(EC2 컨테이너로 가장 저렴). RDS 는 이 교육 계정에선 막혀 있을 수 있으니 컨테이너 방식 권장.
3. `docker-compose.yml`의 `SPRING_DATASOURCE_*` env 주석 해제 + 값 설정
4. 시크릿(Google OAuth, Kakao 키 등)은 EC2 env / compose 로 주입, **코드 하드코딩 금지**
5. CI 테스트도 DB 필요 → `deploy.yml` 의 `test` 잡에 MySQL 서비스 컨테이너 추가

## 트러블슈팅

- **EC2 에서 이미지 pull 실패(unauthorized)**: 배포 스크립트의 GHCR 로그인 단계 확인. 패키지가 조직 소속이라 접근 안 되면, 저장소 *Packages → Package settings* 에서 해당 repo 에 권한 부여 또는 **가시성 Public** 으로 변경.
- **GHCR 이름 오류**: 이미지 경로는 **소문자만** 허용 (`ghcr.io/basecampgsm/basecamp_server`).
- **SSH timeout**: 보안그룹 22 인바운드, `EC2_HOST`, `EC2_SSH_KEY`(전체 내용) 확인.
- **컨테이너 OOM**: swap 설정 확인. 힙은 Dockerfile `JAVA_OPTS`(MaxRAMPercentage)로 제한 중.
- **test 잡 DataSource 실패**: 스켈레톤 단계에선 `application.properties` 자동설정 제외 유지 필수 (Boot 4 경로: `org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration`).

## 참고 파일
- 파이프라인: [.github/workflows/deploy.yml](../.github/workflows/deploy.yml)
- 이미지 빌드: [Dockerfile](../Dockerfile)
- 실행 정의: [docker-compose.yml](../docker-compose.yml)
