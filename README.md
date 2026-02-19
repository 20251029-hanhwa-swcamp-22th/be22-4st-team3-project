# 4th-study

## Local Run

### Backend
```bash
cd backend
./gradlew bootRun
```

사전 설정:
- `backend/.env.example` 참고하여 `backend/.env` 생성

### Frontend
```bash
cd frontend
npm install
npm run dev
```

## Test

### Backend
```bash
cd backend
./gradlew test
```

상세 테스트 문서:
- `backend/README.md`

## Core API Summary

### Auth
- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `POST /api/auth/refresh`

### Account
- `GET /api/accounts`
- `GET /api/accounts/{id}`
- `GET /api/accounts/summary`
- `POST /api/accounts`
- `PUT /api/accounts/{id}`
- `DELETE /api/accounts/{id}`

### Category
- `GET /api/categories`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`

### Transaction
- `GET /api/transactions`
- `GET /api/transactions/summary/{year}/{month}`
- `GET /api/transactions/daily/{year}/{month}`
- `POST /api/transactions`
- `PUT /api/transactions/{id}`
- `DELETE /api/transactions/{id}`

### Dashboard
- `GET /api/dashboard`

