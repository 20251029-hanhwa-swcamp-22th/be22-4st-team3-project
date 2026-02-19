# TODO_REQUIREMENT (MVP 기준 정리)

기준 문서:
- `MVP_GUIDE.md` (우선순위 기준)
- `backend/requirement.md` (확장 요구사항 참고)

## 1. MVP 백엔드 핵심
- [x] 회원가입/로그인 API 구현 (`/api/auth/signup`, `/api/auth/login`)
- [x] JWT 발급/파싱/검증 구현 (`JwtTokenProvider`)
- [x] JWT 인증 필터 구현 (`JwtAuthenticationFilter`)
- [x] Security + CORS 정책 적용 (`SecurityConfig`)
- [x] 계좌 목록/생성/수정/삭제 API 구현 (`/api/accounts`)
- [x] 거래 목록/생성/수정/삭제 API 구현 (`/api/transactions`)
- [x] 카테고리 조회 API 구현 (`/api/categories`)
- [x] MVP 가이드 정합성 맞추기: 인증 엔드포인트 명세 통일 (`/register` vs `/signup`)
  - 우선 signup 으로 유지
- [x] MVP 가이드 정합성 맞추기: 계좌 단건 조회 API 추가 (`GET /api/accounts/{id}`)
- [x] MVP 가이드 정합성 맞추기: 대시보드 통합 API 추가 (`GET /api/dashboard`)

## 2. MVP 비즈니스 규칙
- [x] 본인 데이터 접근 검증 (`userEmail` 기반 소유권 검사)
- [x] 카테고리 삭제 시 참조 거래 존재하면 409 반환
- [x] 거래 금액 `amount >= 1` 검증
- [x] 거래 조회 기본 기간: 당월 1일 ~ 오늘
- [x] 비밀번호 BCrypt 암호화 저장
- [x] 카테고리 정책 확정: 현재 사용자 CRUD 정책 유지
- [x] (읽기 전용 채택 시) 카테고리 CUD 엔드포인트 비활성화/권한 제한 (N/A)
- [x] (읽기 전용 채택 시) 기본 카테고리 시드/상수 제공 (N/A)

## 3. MVP 프론트엔드
- [x] 로그인/회원가입 페이지 구현
- [x] 라우트 가드 구현 (`router.beforeEach`)
- [x] 토큰 저장 + 요청 인터셉터 적용
- [x] 401 응답 처리 인터셉터 적용
- [x] 거래 CRUD + 기간 필터 화면 구현
- [x] 카테고리 CRUD 화면 구현
- [x] 대시보드 화면 구현
- [x] MVP 가이드 정합성 맞추기: `/accounts` 독립 라우트/화면 제공
  - 독립 화면 제공 안함 -> route 제공 안함, 대시보드 내 우측 화면에서 계좌 리스트로 보여줄 예정
- [x] 회원가입 화면 보완: 비밀번호 확인 필드 추가
- [x] 로그아웃 시 서버 `/api/auth/logout` 호출 후 클라이언트 상태 정리
- [x] 대시보드에 "최근 5개 거래" 영역 명시적 노출
- [ ] (선택) MVP 가이드 준수용 Tailwind 도입 여부 결정 및 문서 반영

## 4. DB/매퍼/설정
- [x] MyBatis XML 쿼리 파일 구성 (`src/main/resources/mapper/*.xml`)
- [x] 엔티티 기반 FK/UNIQUE/NOT NULL 제약 반영
- [x] 명시적 DDL 문서화 (`schema.sql` 또는 README 테이블 정의)
- [x] 인덱스 점검 및 추가 (조회 빈도 컬럼 기준: `user_id`, 날짜, 조합 인덱스)
- [x] 환경변수 분리 (`application-local.yml`, `.env.example`, 민감정보 제거)

## 5. 테스트/문서 (MVP 출고 전 필수)
- [x] 서비스 단위 테스트 (Auth/Account/Transaction 우선)
- [x] API 통합 테스트 (로그인 -> 보호 API 호출 흐름)
- [x] 보안/예외 테스트 (401/403/409/검증 실패)
- [x] README 실행 가이드 업데이트 (로컬 백엔드/프론트 실행 + API 요약)
- [ ] MVP 범위 외 기능(예산/리포트/CSV/반복거래)은 명시적으로 제외 표기

## 6. MVP 이후 백로그
- [ ] Dockerfile (backend/frontend)
- [ ] `docker-compose.yml` (app + db)
- [ ] Kubernetes 매니페스트 (deployment/service/pvc/configmap/secret/ingress)
- [ ] 고급 거래 검색/필터/페이지네이션
- [ ] 예산/리포트/CSV/반복 거래 기능
