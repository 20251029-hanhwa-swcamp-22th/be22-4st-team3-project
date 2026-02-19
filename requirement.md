# 1. 주제 : 개인 가계부 프로그램

# 2. 사용 기술 스택
## 2.1 Backend
### - Java 21
### - Spring Boot 3.x 이상
### - Spring Security
### - JWT
### - JPA / MyBatis

 간단한 CUD: JPA

 복잡한 R: MyBatis

## 2.2 Frontend
### - Vue 3

## 2.3 Database
### - MariaDB

## 2.4 CI/CD
### - Docker
### - Kubernetes

# 3. Architecture
## 3.1 Monolithic
- 간단한 구현을 위해 MSA 대신 Monolithic으로 프로그램 구현

---

# 4. 기능 요구사항

## 4.1 회원 관리 (Auth)
| 기능 | 설명 |
|------|------|
| 회원가입 | 이메일, 비밀번호, 닉네임으로 가입 |
| 로그인 | 이메일 + 비밀번호 → JWT Access Token 발급 |
| 로그아웃 | 클라이언트 측 토큰 삭제 |

## 4.2 카테고리 관리
| 기능 | 설명 |
|------|------|
| 카테고리 목록 조회 | 사용자별 수입/지출 카테고리 목록 |
| 카테고리 생성 | 카테고리명 + 유형(INCOME/EXPENSE) 등록 |
| 카테고리 수정 | 카테고리명 변경 |
| 카테고리 삭제 | 해당 카테고리에 거래가 없을 때만 삭제 가능 |

## 4.3 거래 내역 관리
| 기능 | 설명 |
|------|------|
| 거래 등록 | 날짜, 유형(수입/지출), 카테고리, 금액, 메모 입력 |
| 거래 수정 | 기존 거래 내용 변경 |
| 거래 삭제 | 거래 내역 삭제 |
| 거래 목록 조회 | 기간별(시작일~종료일) 내역 조회, 최신순 정렬 |

---

# 5. ERD (Entity Relationship Diagram)

## 5.1 테이블 설계

### users (회원)
| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 회원 고유 ID |
| email | VARCHAR(100) | UNIQUE, NOT NULL | 이메일 (로그인 ID) |
| password | VARCHAR(255) | NOT NULL | 암호화된 비밀번호 |
| nickname | VARCHAR(50) | NOT NULL | 닉네임 |
| created_at | DATETIME | NOT NULL, DEFAULT NOW() | 가입일시 |
| updated_at | DATETIME | NOT NULL, DEFAULT NOW() | 수정일시 |

### categories (카테고리)
| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 카테고리 고유 ID |
| user_id | BIGINT | FK → users.id, NOT NULL | 소유 회원 |
| name | VARCHAR(50) | NOT NULL | 카테고리명 (예: 식비, 월급) |
| type | ENUM('INCOME','EXPENSE') | NOT NULL | 수입/지출 구분 |
| created_at | DATETIME | NOT NULL, DEFAULT NOW() | 생성일시 |

### transactions (거래 내역)
| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 거래 고유 ID |
| user_id | BIGINT | FK → users.id, NOT NULL | 소유 회원 |
| category_id | BIGINT | FK → categories.id, NOT NULL | 카테고리 |
| type | ENUM('INCOME','EXPENSE') | NOT NULL | 수입/지출 구분 |
| amount | BIGINT | NOT NULL | 금액 (원 단위) |
| description | VARCHAR(255) | | 메모 |
| transaction_date | DATE | NOT NULL | 거래 날짜 |
| created_at | DATETIME | NOT NULL, DEFAULT NOW() | 생성일시 |
| updated_at | DATETIME | NOT NULL, DEFAULT NOW() | 수정일시 |

## 5.2 관계
```
users (1) ──── (N) categories
users (1) ──── (N) transactions
categories (1) ──── (N) transactions
```

---

# 6. REST API 설계

## 6.1 인증 (Auth)
| Method | URI | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/api/auth/signup` | 회원가입 | X |
| POST | `/api/auth/login` | 로그인 → JWT 발급 | X |

### 요청/응답 상세

**POST /api/auth/signup**
```json
// Request
{ "email": "user@example.com", "password": "password123", "nickname": "홍길동" }
// Response 201
{ "id": 1, "email": "user@example.com", "nickname": "홍길동" }
```

**POST /api/auth/login**
```json
// Request
{ "email": "user@example.com", "password": "password123" }
// Response 200
{ "accessToken": "eyJhbGciOi...", "tokenType": "Bearer" }
```

## 6.2 카테고리
| Method | URI | 설명 | 인증 |
|--------|-----|------|------|
| GET | `/api/categories` | 내 카테고리 목록 조회 | O |
| POST | `/api/categories` | 카테고리 생성 | O |
| PUT | `/api/categories/{id}` | 카테고리 수정 | O |
| DELETE | `/api/categories/{id}` | 카테고리 삭제 | O |

### 요청/응답 상세

**POST /api/categories**
```json
// Request
{ "name": "식비", "type": "EXPENSE" }
// Response 201
{ "id": 1, "name": "식비", "type": "EXPENSE" }
```

## 6.3 거래 내역
| Method | URI | 설명 | 인증 |
|--------|-----|------|------|
| GET | `/api/transactions` | 거래 목록 조회 (기간 필터) | O |
| POST | `/api/transactions` | 거래 등록 | O |
| PUT | `/api/transactions/{id}` | 거래 수정 | O |
| DELETE | `/api/transactions/{id}` | 거래 삭제 | O |

### 요청/응답 상세

**GET /api/transactions?startDate=2026-01-01&endDate=2026-01-31**
```json
// Response 200
[
  {
    "id": 1,
    "type": "EXPENSE",
    "categoryId": 1,
    "categoryName": "식비",
    "amount": 12000,
    "description": "점심 식사",
    "transactionDate": "2026-01-15"
  }
]
```

**POST /api/transactions**
```json
// Request
{
  "categoryId": 1,
  "type": "EXPENSE",
  "amount": 12000,
  "description": "점심 식사",
  "transactionDate": "2026-01-15"
}
// Response 201
{
  "id": 1,
  "type": "EXPENSE",
  "categoryId": 1,
  "categoryName": "식비",
  "amount": 12000,
  "description": "점심 식사",
  "transactionDate": "2026-01-15"
}
```

---

# 7. 백엔드 패키지 구조 (CQRS)

```
com.mycompany._thstudy/
├── Application.java
│
├── common/                                    # 공통 모듈
│   └── dto/
│       └── ApiResponse.java                   # 통일된 API 응답 DTO
│
├── config/                                    # 설정
│   ├── SecurityConfig.java                    # Spring Security 설정
│   └── JpaConfig.java                         # JPA Auditing 설정
│
├── exception/                                 # 예외 처리
│   ├── BusinessException.java                 # 비즈니스 예외
│   ├── ErrorCode.java                         # 에러 코드 정의 (enum)
│   └── GlobalExceptionHandler.java            # 전역 예외 핸들러
│
├── security/                                  # JWT 인증
│   ├── JwtTokenProvider.java                  # 토큰 생성/검증
│   └── JwtAuthenticationFilter.java           # 요청별 토큰 인증 필터
│
├── auth/                                      # 인증 도메인
│   ├── controller/AuthController.java
│   ├── dto/
│   │   ├── request/  (SignupRequest, LoginRequest)
│   │   └── response/ (SignupResponse, LoginResponse)
│   └── service/AuthService.java
│
├── user/                                      # 사용자 도메인
│   └── command/
│       ├── domain/
│       │   ├── aggregate/User.java            # User 엔티티
│       │   └── repository/UserRepository.java # 도메인 인터페이스
│       └── infrastructure/
│           └── repository/JpaUserRepository.java  # JPA 구현체
│
├── category/                                  # 카테고리 도메인 (CQRS)
│   ├── command/                               # ── Command (쓰기: CUD) ──
│   │   ├── application/
│   │   │   ├── controller/CategoryCommandController.java
│   │   │   ├── dto/
│   │   │   │   ├── request/  (CategoryCreateRequest, CategoryUpdateRequest)
│   │   │   │   └── response/ (CategoryCommandResponse)
│   │   │   └── service/CategoryCommandService.java
│   │   ├── domain/
│   │   │   ├── aggregate/    (Category, CategoryType)
│   │   │   └── repository/CategoryRepository.java
│   │   └── infrastructure/
│   │       └── repository/JpaCategoryRepository.java
│   └── query/                                 # ── Query (읽기: R) ──
│       ├── controller/CategoryQueryController.java
│       ├── dto/response/CategoryResponse.java
│       ├── mapper/CategoryMapper.java         # MyBatis 매퍼
│       └── service/CategoryQueryService.java
│
└── transaction/                               # 거래 도메인 (CQRS)
    ├── command/                               # ── Command (쓰기: CUD) ──
    │   ├── application/
    │   │   ├── controller/TransactionCommandController.java
    │   │   ├── dto/
    │   │   │   ├── request/  (TransactionCreateRequest, TransactionUpdateRequest)
    │   │   │   └── response/ (TransactionCommandResponse)
    │   │   └── service/TransactionCommandService.java
    │   ├── domain/
    │   │   ├── aggregate/Transaction.java
    │   │   └── repository/TransactionRepository.java
    │   └── infrastructure/
    │       └── repository/JpaTransactionRepository.java
    └── query/                                 # ── Query (읽기: R) ──
        ├── controller/TransactionQueryController.java
        ├── dto/
        │   ├── request/TransactionSearchRequest.java
        │   └── response/TransactionListResponse.java
        ├── mapper/TransactionMapper.java      # MyBatis 매퍼
        └── service/TransactionQueryService.java
```

### 아키텍처 요약
```
┌──────────┬──────────────────────────────────────┬──────────────────────────────────┐
│   구분   │         Command (쓰기: CUD)          │        Query (읽기: R)           │
├──────────┼──────────────────────────────────────┼──────────────────────────────────┤
│   ORM    │ JPA (Spring Data JPA)                │ MyBatis                          │
├──────────┼──────────────────────────────────────┼──────────────────────────────────┤
│   계층   │ Controller → Service → Repository    │ Controller → Service → Mapper    │
├──────────┼──────────────────────────────────────┼──────────────────────────────────┤
│   패턴   │ DDD (Domain, Aggregate, Infra)       │ 전통적인 3계층                   │
└──────────┴──────────────────────────────────────┴──────────────────────────────────┘
```

---

# 8. 주요 비즈니스 규칙
1. 모든 거래/카테고리는 **본인 소유 데이터만** 접근 가능 (user_id 기반 권한 체크)
2. 카테고리 삭제 시, 해당 카테고리를 참조하는 거래가 있으면 **삭제 불가** (409 Conflict)
3. 금액(amount)은 **1원 이상의 양수**만 허용
4. 거래 목록 조회 시 기간 미지정 시 **당월 1일~오늘** 기본 적용
5. 비밀번호는 **BCrypt**로 암호화 저장