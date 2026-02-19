# MVP 개발 가이드

## MVP 범위

최소 기능 제품(MVP)을 위해 다음 핵심 기능에 집중합니다:

| 기능 | MVP | 정식 버전 |
|------|-----|----------|
| 회원가입/로그인 | ✅ | ✅ |
| 계좌 관리 | ✅ (1가지 유형: 현금) | 다양한 유형 |
| 거래 추가/조회 | ✅ | ✅ |
| 카테고리 | ✅ (하드코딩된 기본값) | 사용자 정의 카테고리 |
| 거래 목록 | ✅ (기본 목록) | 필터, 검색, 페이지네이션 |
| 대시보드 | ✅ (간단한 요약) | 차트, 추세 |
| 예산 | ❌ | ✅ |
| 보고서 | ❌ | ✅ |
| CSV 가져오기 | ❌ | ✅ |
| 반복 거래 | ❌ | ✅ |

---

## 개발 순서

```
Step 1: 백엔드 설정
    ↓
Step 2: 데이터베이스 및 엔티티
    ↓
Step 3: 인증 API
    ↓
Step 4: 핵심 API (계좌, 거래, 카테고리)
    ↓
Step 5: 프론트엔드 설정
    ↓
Step 6: 인증 페이지
    ↓
Step 7: 주요 기능 UI
    ↓
Step 8: 대시보드
```

---

## Step 1: 백엔드 프로젝트 설정

### 1.1 Spring Boot 프로젝트 생성

[start.spring.io](https://start.spring.io)에서 생성하거나 다음 명령 실행:

```bash
mkdir -p backend && cd backend
```

`build.gradle` 생성:
```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.2'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.financemanager'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.mariadb.jdbc:mariadb-java-client:3.3.2'
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

### 1.2 디렉토리 구조 생성

```bash
mkdir -p src/main/java/com/financemanager/{config,controller,service,repository,entity,dto/request,dto/response,security,exception}
mkdir -p src/main/resources
```

### 1.3 application.yml 생성

```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/finance_manager
    username: root
    password: your_password
    driver-class-name: org.mariadb.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MariaDBDialect
        format_sql: true

jwt:
  secret: your-256-bit-secret-key-here-make-it-long-enough
  expiration: 86400000

server:
  port: 8080
```

### 1.4 메인 애플리케이션 클래스 생성

```java
// src/main/java/com/financemanager/FinanceManagerApplication.java
package com.financemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinanceManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinanceManagerApplication.class, args);
    }
}
```

### 1.5 MariaDB 설정

```sql
CREATE DATABASE finance_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 1.6 설정 확인

```bash
./gradlew bootRun
```

http://localhost:8080 에서 시작되어야 합니다.

---

## Step 2: 데이터베이스 엔티티 (MVP)

다음 순서로 엔티티를 생성합니다:

### 2.1 User 엔티티
```java
// entity/User.java
@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

### 2.2 Account 엔티티
```java
// entity/Account.java
@Entity
@Table(name = "accounts")
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

### 2.3 Transaction 엔티티
```java
// entity/Transaction.java
@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;  // INCOME, EXPENSE

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private BigDecimal amount;

    private String description;

    @Column(nullable = false)
    private LocalDate date;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

// entity/TransactionType.java
public enum TransactionType {
    INCOME, EXPENSE
}
```

---

## Step 3: 인증 API

### 3.1 구현 순서:

1. **JwtTokenProvider** - JWT 토큰 생성/검증
2. **UserRepository** - 사용자 데이터베이스 접근
3. **AuthService** - 회원가입 및 로그인 로직
4. **AuthController** - REST 엔드포인트
5. **SecurityConfig** - Spring Security 설정
6. **JwtAuthenticationFilter** - 토큰 인터셉트 및 검증

### 3.2 MVP 인증 엔드포인트

| 엔드포인트 | 기능 |
|-----------|------|
| `POST /api/auth/register` | 새 사용자 생성 |
| `POST /api/auth/login` | JWT 토큰 발급 |

### 3.3 인증 테스트

```bash
# 회원가입
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password123","name":"Test"}'

# 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password123"}'
```

---

## Step 4: 핵심 API

### 4.1 계좌 API

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/api/accounts` | 사용자 계좌 목록 |
| POST | `/api/accounts` | 계좌 생성 |
| GET | `/api/accounts/{id}` | 계좌 상세 조회 |
| DELETE | `/api/accounts/{id}` | 계좌 삭제 |

### 4.2 거래 API

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/api/transactions` | 거래 목록 |
| POST | `/api/transactions` | 거래 생성 |
| DELETE | `/api/transactions/{id}` | 거래 삭제 |

### 4.3 카테고리 API (MVP: 읽기 전용 하드코딩)

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/api/categories` | 기본 카테고리 목록 |

MVP용 하드코딩 카테고리:
```java
List<String> EXPENSE_CATEGORIES = List.of(
    "음식", "교통", "쇼핑", "공과금", "엔터테인먼트", "기타"
);
List<String> INCOME_CATEGORIES = List.of(
    "급여", "보너스", "투자", "기타"
);
```

### 4.4 대시보드 API

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/api/dashboard` | 요약 데이터 조회 |

응답:
```json
{
  "totalBalance": 1500.00,
  "monthlyIncome": 3000.00,
  "monthlyExpense": 1500.00,
  "recentTransactions": [...]
}
```

---

## Step 5: 프론트엔드 설정

### 5.1 Vue 프로젝트 생성

```bash
npm create vite@latest frontend -- --template vue
cd frontend
npm install
npm install vue-router@4 pinia axios
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

### 5.2 Tailwind 설정

```js
// tailwind.config.js
export default {
  content: ["./index.html", "./src/**/*.{vue,js,ts}"],
  theme: { extend: {} },
  plugins: [],
}
```

```css
/* src/style.css */
@tailwind base;
@tailwind components;
@tailwind utilities;
```

### 5.3 프로젝트 구조

```
frontend/src/
├── main.js
├── App.vue
├── views/
│   ├── Login.vue
│   ├── Register.vue
│   ├── Dashboard.vue
│   ├── Accounts.vue
│   └── Transactions.vue
├── components/
│   ├── Navbar.vue
│   └── TransactionForm.vue
├── stores/
│   └── auth.js
├── api/
│   └── index.js
└── router/
    └── index.js
```

### 5.4 Axios 설정

```js
// src/api/index.js
import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api'
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default api
```

### 5.5 라우터 설정

```js
// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/register', component: () => import('../views/Register.vue') },
  { path: '/', component: () => import('../views/Dashboard.vue'), meta: { auth: true } },
  { path: '/accounts', component: () => import('../views/Accounts.vue'), meta: { auth: true } },
  { path: '/transactions', component: () => import('../views/Transactions.vue'), meta: { auth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.auth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
```

---

## Step 6: 인증 페이지

### 6.1 로그인 페이지
- 이메일 입력
- 비밀번호 입력
- 로그인 버튼
- 회원가입 링크

### 6.2 회원가입 페이지
- 이름 입력
- 이메일 입력
- 비밀번호 입력
- 비밀번호 확인 입력
- 회원가입 버튼
- 로그인 링크

---

## Step 7: 주요 기능 UI

### 7.1 계좌 페이지
- 잔액이 표시된 계좌 목록
- "계좌 추가" 버튼
- 간단한 폼: 이름만 (MVP용)

### 7.2 거래 페이지
- 거래 목록 (날짜, 카테고리, 금액, 설명)
- "거래 추가" 버튼
- 폼: 유형, 카테고리 드롭다운, 금액, 설명, 날짜

---

## Step 8: 대시보드

### 8.1 간단한 대시보드
- 총 잔액 카드
- 이번 달 수입 카드
- 이번 달 지출 카드
- 최근 5개 거래 목록

---

## MVP 체크리스트

### 백엔드
- [ ] Gradle로 프로젝트 설정
- [ ] MariaDB 연결 동작 확인
- [ ] User 엔티티 + 리포지토리
- [ ] Account 엔티티 + 리포지토리
- [ ] Transaction 엔티티 + 리포지토리
- [ ] JWT 인증
- [ ] 인증 엔드포인트 (회원가입, 로그인)
- [ ] 계좌 CRUD 엔드포인트
- [ ] 거래 CRUD 엔드포인트
- [ ] 카테고리 엔드포인트 (하드코딩)
- [ ] 대시보드 엔드포인트
- [ ] CORS 설정

### 프론트엔드
- [ ] Vite로 Vue 프로젝트 설정
- [ ] Tailwind CSS 설정
- [ ] Vue Router 설정
- [ ] Pinia 인증 스토어
- [ ] 토큰 인터셉터가 있는 Axios
- [ ] 로그인 페이지
- [ ] 회원가입 페이지
- [ ] 대시보드 페이지
- [ ] 계좌 페이지
- [ ] 거래 페이지
- [ ] 네비게이션 바
- [ ] 라우트 가드

### 통합
- [ ] 프론트엔드에서 사용자 등록 가능
- [ ] 프론트엔드에서 로그인 및 토큰 저장 가능
- [ ] 프론트엔드에서 계좌 생성/조회 가능
- [ ] 프론트엔드에서 거래 생성/조회 가능
- [ ] 대시보드에 실제 데이터 표시

---

## 빠른 명령어 참조

```bash
# 백엔드 시작
cd backend && ./gradlew bootRun

# 프론트엔드 시작
cd frontend && npm run dev

# 백엔드 실행 주소: http://localhost:8080
# 프론트엔드 실행 주소: http://localhost:5173
```

---

## MVP 이후 다음 단계

1. 거래 필터링 및 검색 추가
2. 사용자 정의 카테고리 구현
3. 예산 추적 추가
4. 차트 및 보고서 구축
5. 반복 거래 추가
6. CSV 가져오기 구현
7. 이메일 인증 추가
8. 오류 처리 개선
9. 테스트 작성
10. 프로덕션 배포
