# Account Book (가계부)

수입, 지출, 계좌, 카테고리를 관리하고 시각적 대시보드로 확인할 수 있는 개인 재무 관리 애플리케이션입니다.

## 기술 스택

### Backend
<p>
  <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"/>
  <img src="https://img.shields.io/badge/JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white"/>
  <img src="https://img.shields.io/badge/MyBatis-000000?style=for-the-badge&logo=mybatis&logoColor=white"/>
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white"/>
</p>

### Frontend
<p>
  <img src="https://img.shields.io/badge/Vue_3-4FC08D?style=for-the-badge&logo=vuedotjs&logoColor=white"/>
  <img src="https://img.shields.io/badge/Pinia-FFD43B?style=for-the-badge&logo=vuedotjs&logoColor=black"/>
  <img src="https://img.shields.io/badge/Vue_Router-4FC08D?style=for-the-badge&logo=vuedotjs&logoColor=white"/>
  <img src="https://img.shields.io/badge/Chart.js-FF6384?style=for-the-badge&logo=chartdotjs&logoColor=white"/>
  <img src="https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white"/>
  <img src="https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white"/>
</p>

### Database
<p>
  <img src="https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white"/>
</p>

### DevOps / Infra
<p>
  <img src="https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white"/>
  <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white"/>
  <img src="https://img.shields.io/badge/DockerHub-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
</p>

### Monitoring / Logging
<p>
  <img src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white"/>
  <img src="https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white"/>
  <img src="https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white"/>
  <img src="https://img.shields.io/badge/Logstash-005571?style=for-the-badge&logo=logstash&logoColor=white"/>
  <img src="https://img.shields.io/badge/Kibana-005571?style=for-the-badge&logo=kibana&logoColor=white"/>
</p>

## 프로젝트 구조

```
be22-4st-team3-project/
├── backend/                  # Spring Boot API 서버
│   ├── src/main/java/        # DDD + CQRS 도메인 모듈
│   │   └── com/mycompany/_thstudy/
│   │       ├── auth/         # JWT 인증 (회원가입, 로그인, 토큰 갱신, 로그아웃)
│   │       ├── account/      # 계좌 관리 (CRUD)
│   │       ├── category/     # 수입/지출 카테고리 관리
│   │       ├── transaction/  # 거래 내역 + 검색/내보내기
│   │       ├── dashboard/    # 대시보드 집계 데이터
│   │       ├── config/       # Security, JPA, CORS 설정
│   │       ├── security/     # JWT 필터 & 프로바이더
│   │       └── exception/    # 중앙 집중식 에러 처리
│   ├── src/main/resources/
│   │   ├── application.yml   # 앱 설정 + Actuator
│   │   ├── logback-spring.xml # Logstash 로그 전송
│   │   └── mapper/           # MyBatis XML 쿼리 매퍼
│   ├── Dockerfile            # 멀티 스테이지 빌드 (Gradle → JRE)
│   └── build.gradle
├── frontend/                 # Vue 3 SPA
│   ├── src/
│   │   ├── views/            # 페이지 컴포넌트 (인증, 대시보드, 거래, 카테고리)
│   │   ├── components/       # 재사용 UI (차트, 캘린더, 레이아웃)
│   │   ├── stores/           # Pinia 상태 관리
│   │   ├── api/              # Axios 인스턴스 + JWT 인터셉터
│   │   └── router/           # Vue Router + 인증 가드
│   ├── nginx.conf            # Nginx 리버스 프록시 템플릿 (envsubst)
│   └── Dockerfile            # 멀티 스테이지 빌드 (Node → Nginx)
├── docker-compose.yml        # 로컬 개발: 모든 서비스 한 번에 실행
├── docker/                   # Docker 전용 설정
│   ├── prometheus/prometheus.yml
│   ├── logstash/logstash.conf
│   └── grafana/provisioning/datasources/datasource.yml
├── devOps/                   # CI/CD & 운영 도구
│   ├── jenkins-pipeline-script.md  # Jenkins 파이프라인 정의
│   ├── cicd-and-monitoring.md      # DevOps 상세 문서
│   ├── traffic-test.sh             # Grafana 테스트용 트래픽 생성기
│   └── cpu-stress-test.sh          # 모니터링용 CPU 스트레스 테스트
└── sql/
    └── account_book_ddl.sql  # 데이터베이스 스키마
```

K8s 매니페스트는 CI/CD 웹훅 무한루프 방지를 위해 **별도 저장소**로 관리됩니다:
```
be22-4st-team3-k8s-manifest/
├── boot-deployment.yml         # 백엔드 (3 레플리카) + Service
├── vue-deployment.yml          # 프론트엔드 (1 레플리카) + Service
├── ingress.yml                 # Nginx Ingress 라우팅 규칙
├── backend-config.yaml         # ConfigMap (DB URL, Logstash 설정)
├── backend-secret.yml          # Secret (DB 자격증명, JWT)
├── elasticsearch.yml           # Elasticsearch + Service
├── logstash.yml                # Logstash + Service
├── kibana.yml                  # Kibana + Service
├── prometheus.yml              # Prometheus + Service
├── grafana.yml                 # Grafana + Service
├── configmap-logstash.yml      # Logstash 파이프라인 설정
├── configmap-prometheus.yml    # Prometheus 스크레이프 설정
├── configmap-grafana.yml       # Grafana 데이터소스 프로비저닝
├── pvc.yml                     # PersistentVolumeClaim (ES + Grafana)
└── jenkins-proxy.yml           # ExternalName Service → 호스트 Jenkins
```

## 아키텍처

### 백엔드 (DDD + CQRS)

각 도메인 모듈은 **Command** (JPA를 통한 쓰기)와 **Query** (MyBatis를 통한 읽기)를 분리합니다:

```
module/
├── command/
│   ├── application/
│   │   ├── controller/    # REST 쓰기 엔드포인트
│   │   ├── service/       # 비즈니스 로직
│   │   └── dto/           # 요청/응답 DTO
│   └── domain/
│       ├── aggregate/     # JPA 엔티티
│       └── repository/    # Spring Data 리포지토리
└── query/
    ├── controller/        # REST 읽기 엔드포인트
    ├── service/           # 쿼리 서비스 (MyBatis)
    └── dto/               # 쿼리 응답 DTO
```

### 인증

JWT 기반 무상태 인증:
- **Access Token** (24시간) + **Refresh Token** (7일)
- Refresh Token은 `refresh_tokens` DB 테이블에 저장
- 프론트엔드: localStorage + Axios 인터셉터로 401 응답 시 자동 갱신

### CI/CD 파이프라인 (Jenkins)

```
[Git Push] → [GitHub Webhook] → [Jenkins]
                                    │
                              1. Checkout (변경 폴더 감지)
                              2. Source Build (백엔드만, 변경 시)
                              3. Docker Build & Push → DockerHub
                              4. K8S Manifest Update (별도 저장소)
                              5. K8S Deploy (kubectl set image)
```

- **조건부 빌드**: backend/frontend 폴더에 변경이 있을 때만 빌드
- **2개 저장소 전략**: 매니페스트 업데이트를 별도 저장소에 push하여 웹훅 무한루프 방지
- 크로스 플랫폼 지원 (Unix + Windows)

### Kubernetes 배포 구조

```
                      [Nginx Ingress :80]
                             │
          ┌──────────┬───────┴────────┬───────────┐
          │          │                │           │
      /api/*    /grafana/*      /kibana/*     /*
          │          │                │           │
    boot-service  grafana-svc   kibana-svc   vue-service
    :8001 (x3)    :3000          :5601        :8000
```

### 모니터링 & 로깅

```
[Spring Boot] ──actuator/prometheus──→ [Prometheus] ──→ [Grafana]
[Spring Boot] ──logback TCP JSON────→ [Logstash]   ──→ [Elasticsearch] ──→ [Kibana]
```

## 시작하기

### 사전 요구사항

- Java 21, Node 18+, MariaDB 10+
- Docker Desktop (K8s 배포 시 Kubernetes 활성화 필요)

### 로컬 개발

```bash
# 1. 데이터베이스
mysql -u root -p < sql/account_book_ddl.sql

# 2. 백엔드
cp backend/.env.example backend/.env  # DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET 입력
cd backend && ./gradlew bootRun

# 3. 프론트엔드 (다른 터미널에서)
cd frontend && npm install && npm run dev
```

프론트엔드는 `http://localhost:5173`에서 실행되며, `/api` 요청을 `http://localhost:8080`으로 프록시합니다.

### Docker Compose (전체 스택)

```bash
docker-compose up -d --build
```

모니터링 포함 전체 서비스:
- 앱: `http://localhost`
- Grafana: `http://localhost:3000`
- Kibana: `http://localhost:5601`
- Prometheus: `http://localhost:9090`

### Kubernetes 배포

```bash
# 매니페스트 저장소에서 전체 적용
kubectl apply -f /path/to/be22-4st-team3-k8s-manifest/

# 상태 확인
kubectl get pods
```

Ingress를 통한 접속 (`http://localhost`):
- 앱: `http://localhost/`
- 백엔드 API: `http://localhost/boot/api/...`
- Grafana: `http://localhost/grafana`
- Kibana: `http://localhost/kibana`

## API 엔드포인트

### 인증 (Auth)
| 메서드 | 엔드포인트 | 설명 |
|---|---|---|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 (JWT 토큰 반환) |
| POST | `/api/auth/logout` | 로그아웃 (Refresh Token 무효화) |
| POST | `/api/auth/refresh` | Access Token 갱신 |

### 계좌 (Account)
| 메서드 | 엔드포인트 | 설명 |
|---|---|---|
| GET | `/api/accounts` | 전체 계좌 목록 |
| GET | `/api/accounts/{id}` | 계좌 상세 조회 |
| GET | `/api/accounts/summary` | 계좌 잔액 요약 |
| POST | `/api/accounts` | 계좌 생성 |
| PUT | `/api/accounts/{id}` | 계좌 수정 |
| DELETE | `/api/accounts/{id}` | 계좌 삭제 |

### 카테고리 (Category)
| 메서드 | 엔드포인트 | 설명 |
|---|---|---|
| GET | `/api/categories` | 전체 카테고리 목록 |
| POST | `/api/categories` | 카테고리 생성 |
| PUT | `/api/categories/{id}` | 카테고리 수정 |
| DELETE | `/api/categories/{id}` | 카테고리 삭제 |

### 거래 (Transaction)
| 메서드 | 엔드포인트 | 설명 |
|---|---|---|
| GET | `/api/transactions` | 거래 검색 (필터: 날짜, 계좌, 유형, 카테고리, 키워드, 금액) |
| GET | `/api/transactions/summary/{year}/{month}` | 월별 요약 |
| GET | `/api/transactions/daily/{year}/{month}` | 일별 내역 |
| GET | `/api/transactions/export/csv` | CSV 내보내기 |
| GET | `/api/transactions/export/xlsx` | Excel 내보내기 |
| POST | `/api/transactions` | 거래 생성 |
| PUT | `/api/transactions/{id}` | 거래 수정 |
| DELETE | `/api/transactions/{id}` | 거래 삭제 |

### 대시보드 (Dashboard)
| 메서드 | 엔드포인트 | 설명 |
|---|---|---|
| GET | `/api/dashboard` | 대시보드 집계 데이터 |

## 외부 접속 (ngrok)

ngrok을 사용하여 로컬 K8s 클러스터를 외부에 공개합니다.

```bash
ngrok http 80
```

| 항목 | 값 |
|---|---|
| 포워딩 URL | `https://undisputatiously-unfamiliarized-necole.ngrok-free.dev` |
| 로컬 대상 | `http://localhost:80` (K8s Ingress) |
| 리전 | Japan (jp) |
| 웹 인터페이스 | `http://127.0.0.1:4040` |

외부 접속 경로:
- 앱: `https://undisputatiously-unfamiliarized-necole.ngrok-free.dev/`
- Grafana: `https://undisputatiously-unfamiliarized-necole.ngrok-free.dev/grafana`
- Kibana: `https://undisputatiously-unfamiliarized-necole.ngrok-free.dev/kibana`
- GitHub Webhook: `https://undisputatiously-unfamiliarized-necole.ngrok-free.dev/github-webhook/`