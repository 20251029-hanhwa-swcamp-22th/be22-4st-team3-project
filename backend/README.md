# Backend Guide

## 1. Prerequisites
- Java 17
- MariaDB 10+
- Gradle Wrapper (`gradlew.bat`)

## 2. Environment Setup
1. `backend/.env.example` 파일을 참고해 `backend/.env` 생성
2. 최소 필수 값:
   - `DB_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
   - `JWT_SECRET`

기본 프로파일은 `local`이며, `application.yml`에서 `.env`를 자동 import합니다.

## 3. Run
```bash
cd backend
./gradlew bootRun
```

## 4. Build And Test
```bash
cd backend
./gradlew test
```

테스트 리포트:
- `backend/build/reports/tests/test/index.html`

## 5. Database Schema
- 명시적 DDL: `backend/src/main/resources/schema.sql`
- 핵심 테이블:
  - `users`
  - `accounts`
  - `categories`
  - `transactions`
  - `refresh_tokens`

## 6. Test Documentation

### 6.1 Unit Test Targets
- `AuthCommandService`
  - 회원가입 시 비밀번호 암호화 저장
  - 로그인 성공/실패
  - refresh token 재발급 로직
- `AccountCommandService`
  - 생성/수정/삭제 정상 케이스
  - 타 사용자 계좌 접근 시 `ACCESS_DENIED`
- `TransactionCommandService`
  - `amount >= 1` 검증
  - 카테고리 타입 불일치 시 예외
  - 타 사용자 거래 수정/삭제 차단

### 6.2 API Integration Test Targets
- 인증 흐름:
  1. `POST /api/auth/signup`
  2. `POST /api/auth/login`
  3. Access token으로 보호 API 호출
- 대시보드:
  - `GET /api/dashboard` 응답 필드 검증
  - `totalBalance`, `monthlyIncome`, `monthlyExpense`, `recentTransactions`

### 6.3 Security/Exception Test Targets
- 인증/인가:
  - 토큰 없음 -> 401
  - 만료/변조 토큰 -> 401
  - 본인 소유 아님 -> 403
- 비즈니스 예외:
  - 카테고리 삭제 시 참조 거래 존재 -> 409
  - 중복 카테고리 -> 409
  - 잘못된 입력값 -> 400

## 7. Current Test Status
- 현재 자동화 테스트는 스프링 컨텍스트 로드 테스트 중심입니다.
- 위 6장 항목을 기준으로 서비스 단위/통합/보안 테스트를 순차 확장합니다.


## 8. DB Bootstrap (after git pull)
```bash
mysql -u root -p < backend/sql/account_book_ddl.sql
```
