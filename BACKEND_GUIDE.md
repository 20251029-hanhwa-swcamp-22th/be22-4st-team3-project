# Backend 코드 작성 가이드 (CQRS)

> 각 클래스의 TODO를 구현할 때 참고하는 가이드입니다.
> **권장 구현 순서**: 1단계(Config/Security) → 2단계(Aggregate) → 3단계(Command Service) → 4단계(Query Service) → 5단계(Controller) → 6단계(ExceptionHandler)

---

## CQRS 아키텍처 요약

```
┌──────────┬──────────────────────────────────────┬──────────────────────────────────┐
│   구분   │         Command (쓰기: CUD)          │        Query (읽기: R)           │
├──────────┼──────────────────────────────────────┼──────────────────────────────────┤
│   ORM    │ JPA (Spring Data JPA)                │ MyBatis                          │
├──────────┼──────────────────────────────────────┼──────────────────────────────────┤
│   계층   │ Controller → Service → Repository    │ Controller → Service → Mapper    │
├──────────┼──────────────────────────────────────┼──────────────────────────────────┤
│   패턴   │ DDD (Domain, Aggregate, Infra)       │ 전통적인 3계층                   │
├──────────┼──────────────────────────────────────┼──────────────────────────────────┤
│ Repository│ domain/repository (인터페이스)       │ query/mapper (MyBatis Mapper)    │
│          │ infrastructure/repository (JPA 구현)  │                                  │
└──────────┴──────────────────────────────────────┴──────────────────────────────────┘
```

**핵심 포인트**: Command 측은 JPA로 엔티티 중심의 쓰기 작업을, Query 측은 MyBatis로 SQL 중심의 읽기 작업을 분리하여 처리합니다.

---

## 1단계: Config & Security

### 1-1. `config/SecurityConfig.java`
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class)
        .build();
}

@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### 1-2. `security/JwtTokenProvider.java`
```
필요한 필드:
- @Value("${jwt.secret}") String secret
- @Value("${jwt.expiration}") long expiration
- SecretKey secretKey → @PostConstruct에서 Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))

메서드별 구현:
- createToken(Long userId)
  → Jwts.builder().subject(userId.toString()).issuedAt(now).expiration(now+exp).signWith(key).compact()

- getUserIdFromToken(String token)
  → Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject() → Long

- validateToken(String token)
  → try { 파싱 → true } catch (JwtException) { false }
```

### 1-3. `security/JwtAuthenticationFilter.java`
```
doFilterInternal 흐름:
1. request.getHeader("Authorization") → "Bearer " 확인
2. token 추출 → validateToken → getUserIdFromToken
3. UsernamePasswordAuthenticationToken(userId, null, List.of())
4. SecurityContextHolder.getContext().setAuthentication(auth)
5. filterChain.doFilter(request, response)
```

### 1-4. `config/JpaConfig.java` — 완료 (JPA Auditing 활성화)

---

## 2단계: Aggregate (엔티티) 완성

### 2-1. `user/command/domain/aggregate/User.java` — 완료
- `@EntityListeners(AuditingEntityListener.class)` + `@CreatedDate`, `@LastModifiedDate` 사용
- 별도 `@PrePersist` 불필요 (JPA Auditing 자동 처리)

### 2-2. `category/command/domain/aggregate/Category.java`
```java
public void updateName(String name) {
    this.name = name;
}
```

### 2-3. `transaction/command/domain/aggregate/Transaction.java`
```java
public void update(Category category, CategoryType type, Long amount, String description, LocalDate transactionDate) {
    this.category = category;
    this.type = type;
    this.amount = amount;
    this.description = description;
    this.transactionDate = transactionDate;
}
```

---

## 3단계: Command Service 구현 (JPA)

### 3-1. `auth/service/AuthService.java`

| 메서드 | 핵심 로직 |
|--------|-----------|
| `signup` | `existsByEmail()` → `BusinessException(DUPLICATE_EMAIL)` / `passwordEncoder.encode()` → `User.builder().build()` → `save()` → `SignupResponse` |
| `login` | `findByEmail()` → `BusinessException(LOGIN_FAILED)` / `passwordEncoder.matches()` → `jwtTokenProvider.createToken(userId)` → `LoginResponse` |

### 3-2. `category/command/.../CategoryCommandService.java`

| 메서드 | 핵심 로직 |
|--------|-----------|
| `createCategory` | `userRepository.findById` → `Category.builder().user(user).name().type().build()` → `save()` → `CategoryCommandResponse` |
| `updateCategory` | `categoryRepository.findById` → 소유자 확인 `ACCESS_DENIED` → `category.updateName()` → `CategoryCommandResponse` |
| `deleteCategory` | `findById` → 소유자 확인 → `transactionRepository.existsByCategoryId()` → `CATEGORY_HAS_TRANSACTIONS` → `delete()` |

### 3-3. `transaction/command/.../TransactionCommandService.java`

| 메서드 | 핵심 로직 |
|--------|-----------|
| `createTransaction` | `userRepository.findById` → `categoryRepository.findById` + 소유자 확인 → `Transaction.builder().build()` → `save()` |
| `updateTransaction` | `transactionRepository.findById` → 소유자 확인 → `categoryRepository.findById` + 소유자 확인 → `transaction.update()` |
| `deleteTransaction` | `findById` → 소유자 확인 → `delete()` |

**예외는 모두 `throw new BusinessException(ErrorCode.XXX)` 패턴 사용**

---

## 4단계: Query Service 구현 (MyBatis)

### 4-1. `category/query/service/CategoryQueryService.java`
```java
public List<CategoryResponse> getCategories(Long userId) {
    return categoryMapper.findByUserId(userId);
}
```
- SQL: `mapper/CategoryMapper.xml` → `SELECT id, name, type FROM categories WHERE user_id = #{userId} ORDER BY type, name`

### 4-2. `transaction/query/service/TransactionQueryService.java`
```java
public List<TransactionListResponse> getTransactions(Long userId, LocalDate startDate, LocalDate endDate) {
    if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
    if (endDate == null) endDate = LocalDate.now();
    return transactionMapper.findByUserIdAndDateRange(userId, startDate, endDate);
}
```
- SQL: `mapper/TransactionMapper.xml` → `JOIN categories` + `BETWEEN + ORDER BY DESC`

---

## 5단계: Controller 구현

### 공통 패턴
```java
// 인증된 사용자 ID 주입
@AuthenticationPrincipal Long userId

// 응답은 ApiResponse<T>로 통일
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));   // 201
return ResponseEntity.ok(ApiResponse.ok(result));                                      // 200
return ResponseEntity.ok(ApiResponse.noContent());                                     // 204 삭제
```

### API 엔드포인트 분리

| HTTP | URI | Controller | 측면 |
|------|-----|------------|------|
| POST | `/api/auth/signup` | AuthController | - |
| POST | `/api/auth/login` | AuthController | - |
| GET | `/api/categories` | CategoryQueryController | **Query** |
| POST | `/api/categories` | CategoryCommandController | **Command** |
| PUT | `/api/categories/{id}` | CategoryCommandController | **Command** |
| DELETE | `/api/categories/{id}` | CategoryCommandController | **Command** |
| GET | `/api/transactions` | TransactionQueryController | **Query** |
| POST | `/api/transactions` | TransactionCommandController | **Command** |
| PUT | `/api/transactions/{id}` | TransactionCommandController | **Command** |
| DELETE | `/api/transactions/{id}` | TransactionCommandController | **Command** |

---

## 6단계: GlobalExceptionHandler 구현

```java
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
    ErrorCode code = e.getErrorCode();
    return ResponseEntity.status(code.getStatus())
        .body(new ApiResponse<>(code.getStatus(), code.getMessage(), null));
}

@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
    return ResponseEntity.badRequest()
        .body(new ApiResponse<>(400, message, null));
}

@ExceptionHandler(Exception.class)
public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    return ResponseEntity.status(500)
        .body(new ApiResponse<>(500, "서버 내부 오류가 발생했습니다.", null));
}
```

---

## 설정 파일 체크리스트

### `application.yml`
- [ ] `spring.datasource.password` — MariaDB 비밀번호 입력
- [ ] `jwt.secret` — Base64 인코딩된 256비트 이상 시크릿 키
- [ ] MariaDB에 `account_book` DB 생성: `CREATE DATABASE account_book;`

---

## 파일 목록 & 상태

### 공통 / 설정
| 파일 | 상태 | 단계 |
|------|------|------|
| `common/dto/ApiResponse` | TODO | 5단계 |
| `config/SecurityConfig` | TODO | 1단계 |
| `config/JpaConfig` | **완료** | - |
| `security/JwtTokenProvider` | TODO | 1단계 |
| `security/JwtAuthenticationFilter` | TODO | 1단계 |
| `exception/ErrorCode` | **완료** | - |
| `exception/BusinessException` | **완료** | - |
| `exception/GlobalExceptionHandler` | TODO | 6단계 |
| `application.yml` | TODO (비밀번호/시크릿) | 설정 |

### Auth / User
| 파일 | 상태 | 단계 |
|------|------|------|
| `user/command/domain/aggregate/User` | **완료** | - |
| `user/command/domain/repository/UserRepository` | **완료** | - |
| `user/command/infrastructure/.../JpaUserRepository` | **완료** | - |
| `auth/dto/request/*` | **완료** | - |
| `auth/dto/response/*` | **완료** | - |
| `auth/service/AuthService` | TODO | 3단계 |
| `auth/controller/AuthController` | TODO | 5단계 |

### Category (CQRS)
| 파일 | 측면 | 상태 | 단계 |
|------|------|------|------|
| `category/command/domain/aggregate/Category` | Command | TODO (updateName) | 2단계 |
| `category/command/domain/aggregate/CategoryType` | Command | **완료** | - |
| `category/command/domain/repository/CategoryRepository` | Command | **완료** | - |
| `category/command/infrastructure/.../JpaCategoryRepository` | Command | **완료** | - |
| `category/command/.../dto/request/*` | Command | **완료** | - |
| `category/command/.../dto/response/*` | Command | **완료** | - |
| `category/command/.../service/CategoryCommandService` | Command | TODO | 3단계 |
| `category/command/.../controller/CategoryCommandController` | Command | TODO | 5단계 |
| `category/query/dto/response/CategoryResponse` | Query | **완료** | - |
| `category/query/mapper/CategoryMapper` | Query | **완료** | - |
| `category/query/service/CategoryQueryService` | Query | TODO | 4단계 |
| `category/query/controller/CategoryQueryController` | Query | TODO | 5단계 |
| `mapper/CategoryMapper.xml` | Query | **완료** | - |

### Transaction (CQRS)
| 파일 | 측면 | 상태 | 단계 |
|------|------|------|------|
| `transaction/command/domain/aggregate/Transaction` | Command | TODO (update) | 2단계 |
| `transaction/command/domain/repository/TransactionRepository` | Command | **완료** | - |
| `transaction/command/infrastructure/.../JpaTransactionRepository` | Command | **완료** | - |
| `transaction/command/.../dto/request/*` | Command | **완료** | - |
| `transaction/command/.../dto/response/*` | Command | **완료** | - |
| `transaction/command/.../service/TransactionCommandService` | Command | TODO | 3단계 |
| `transaction/command/.../controller/TransactionCommandController` | Command | TODO | 5단계 |
| `transaction/query/dto/request/TransactionSearchRequest` | Query | **완료** | - |
| `transaction/query/dto/response/TransactionListResponse` | Query | **완료** | - |
| `transaction/query/mapper/TransactionMapper` | Query | **완료** | - |
| `transaction/query/service/TransactionQueryService` | Query | TODO | 4단계 |
| `transaction/query/controller/TransactionQueryController` | Query | TODO | 5단계 |
| `mapper/TransactionMapper.xml` | Query | **완료** | - |
