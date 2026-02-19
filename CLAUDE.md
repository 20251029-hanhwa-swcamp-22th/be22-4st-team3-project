# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Personal finance / account book application (가계부). Full-stack with a Spring Boot backend and Vue 3 frontend.

## Common Commands

### Backend (from `backend/`)
```bash
./gradlew bootRun          # Run dev server (port 8080)
./gradlew test             # Run all tests (JUnit 5)
./gradlew test --tests "com.mycompany._thstudy.account.*"  # Run tests for a specific module
./gradlew build            # Build JAR
```

### Frontend (from `frontend/`)
```bash
npm install                # Install dependencies
npm run dev                # Dev server (port 5173, proxies /api to :8080)
npm run build              # Production build
```

### Database
```bash
mysql -u root -p < backend/sql/account_book_ddl.sql   # Bootstrap schema
```

## Environment Setup

Copy `backend/.env.example` to `backend/.env` and fill in: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`. Spring auto-imports `.env` as properties (see `application.yml` config import). Default profile is `local`.

Requires: Java 21, MariaDB 10+, Node 18+.

## Architecture

### Backend — DDD + CQRS

**Base package:** `com.mycompany._thstudy`

Each domain module (`auth`, `user`, `account`, `category`, `transaction`, `dashboard`) follows:
```
module/
├── command/
│   ├── application/
│   │   ├── controller/    # REST endpoints
│   │   ├── service/       # Business logic
│   │   └── dto/           # Request/response DTOs
│   └── domain/
│       ├── aggregate/     # JPA entities (aggregate roots)
│       └── repository/    # Spring Data JPA repositories
└── query/
    ├── controller/        # Read-only endpoints
    ├── service/           # Query services using MyBatis
    └── dto/               # Query response DTOs
```

- **Command side** uses JPA/Spring Data for writes
- **Query side** uses MyBatis XML mappers (`src/main/resources/mapper/*.xml`) for reads
- Cross-cutting: `config/` (SecurityConfig, JpaConfig), `security/` (JWT filter/provider), `exception/` (GlobalExceptionHandler + ErrorCode enum), `common/dto/ApiResponse`

### Frontend — Vue 3 Composition API

- **Router:** `src/router/index.js` — navigation guards enforce auth; lazy-loaded routes
- **State:** Pinia stores in `src/stores/` (auth, account, category, transaction)
- **API layer:** `src/api/index.js` — Axios instance with JWT interceptor and auto-refresh on 401
- **Views:** `src/views/` — page components per domain (auth, dashboard, transaction, category)
- **Components:** `src/components/` — reusable UI (charts via Chart.js, calendar, account list)
- **Path alias:** `@` → `src/`

### API Proxy

Frontend dev server proxies `/api` requests to `http://localhost:8080` (configured in `vite.config.js`). All backend endpoints are under `/api/`.

### Authentication Flow

JWT-based stateless auth. Access token (24h) + refresh token (7d) stored in `refresh_tokens` table. Frontend stores tokens in localStorage, attaches via Axios request interceptor, and auto-retries with refresh on 401.

### Database

MariaDB with 5 tables: `users`, `accounts`, `categories`, `transactions`, `refresh_tokens`. Schema in `backend/src/main/resources/schema.sql`. JPA ddl-auto is `update` in dev. Collation: `utf8mb4_unicode_ci`.

### Error Handling

Centralized via `GlobalExceptionHandler` (`@RestControllerAdvice`). All business errors use `ErrorCode` enum → `BusinessException`. Standard response wrapper: `ApiResponse<T>`.
