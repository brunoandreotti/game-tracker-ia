# Auth v1 Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user - do not proceed without it.**

**Code gate:** Do not start Execute until Bruno explicitly allows generating code (e.g. “pode implementar” / “pode gerar o código”).

---

**Design**: `.specs/features/auth-v1/design.md`
**Status**: Draft (awaiting Bruno approval)

---

## Test Coverage Matrix

> Generated from codebase, project guidelines, and spec - confirm before Execute. Guidelines found: `AGENTS.md`, `docs/stack.md` (unitário / integração / componente; Testcontainers; WireMock; AD-003; AD-004), `.specs/STATE.md` (AD-015, AD-016, AD-017), `frontend/package.json` (Vitest). Samples: `TrackedGameServiceImplSpec`, `JpaTrackedGameRepositorySpec`, `TrackedGameControllerSpec`, `frontend/src/api/apiClient.test.ts`.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Domain / application (`UserServiceImpl`, `OtpAuthServiceImpl`, `TrackedGameServiceImpl`, `SessionServiceImpl`, Google handlers) | unit | 1:1 to relevant AUTH ACs; all listed OTP/isolation/Google branches; no Spring | `src/test/groovy/**/*Spec.groovy` | `mvn test -Dtest=ClassNameSpec` |
| Persistence (`JpaUserRepository`, `JpaEmailOtpRepository`, `JpaTrackedGameRepository`) | integration | Key queries + unique `(user_id, rawg_id)` + OTP save/find/delete on Testcontainers Postgres (AD-003) | `src/test/groovy/**/*Spec.groovy` | `mvn test` (Docker) |
| HTTP / component (`AuthController`, existing diary/search specs, security lock-down) | component | `@SpringBootTest` real controllers/services/repos; Postgres Testcontainers; stub only RAWG WireMock and mail/Google at adapter/port (AD-004). No `@WebMvcTest`. Happy + 400/401/404/409/429/503 | `src/test/groovy/**/*Spec.groovy` | `mvn test` (Docker) |
| Front API / pages / guards | unit (Vitest + Testing Library) | Happy + error + 401 redirect + `/login` two-step + guards; no E2E | `frontend/src/**/*.test.ts(x)` | `cd frontend && npm test` |
| Flyway / entity / port / YAML / pom / Compose / CORS / SecurityConfig class | none | Build gate only | - | `mvn -q package` |
| Study markdown | none | Docs-only | - | none |

## Gate Check Commands

> Generated from `pom.xml` and `frontend/package.json` - confirm before Execute.

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | After domain/application unit specs | `mvn test -Dtest=ClassNameSpec` |
| Quick (front) | After Vitest tasks | `cd frontend && npm test` |
| Full | After persistence, HTTP/component, or security lock-down | `mvn test` (Docker required) |
| Build | After Flyway/entity/config/port, or phase close | `mvn -q package` and/or `cd frontend && npm run build` |
| none | Docs-only study task | none |

---

## Execution Plan

Phases are ordered and run sequentially - each phase completes before the next begins, and tasks within a phase execute in order.

### Phase 1: Schema and user

```
T2 -> T3 -> T5
T2 -> T4 -> T5
T1 -> T5
T3 -> T6
```

### Phase 2: OTP core and mail

```
T7 -> T8 -> T10
T7 -> T9 -> T10
T8 -> T12
T11 -> T12
```

### Phase 3: Security wiring

```
T13 -> T14 -> T17
T13 -> T15 -> T17
T13 -> T16 -> T17
T13 -> T18
```

### Phase 4: Auth HTTP and lock-down

```
T19 -> T20
```

### Phase 5: Diary ownership

```
T21 -> T22 -> T24
T21 -> T23 -> T24
T23 -> T25 -> T27
T23 -> T26 -> T28
T24 -> T27
T24 -> T28
```

### Phase 6: Frontend session

```
T29 -> T30 -> T31 -> T33
T30 -> T32 -> T33
T30 -> T34
```

### Phase 7: Study guide

```
T35
```

---

## Task Breakdown

### Phase 1: Schema and user

### T1: Add Flyway V3 user OTP and diary owner

**What**: Criar `V3__app_user_email_otp_and_tracked_game_user.sql` com `app_user`, `email_otp`, `tracked_game.user_id` nullable (FK), drop de `uq_tracked_game_rawg_id` e unique `(user_id, rawg_id)`. Ajustar o spec JPA de unique global para o suite continuar verde (NULLs no Postgres não colidem).
**Where**: `src/main/resources/db/migration/V3__app_user_email_otp_and_tracked_game_user.sql`
**Depends on**: None
**Reuses**: `V1__tracked_games_and_sessions.sql`, `V2__rating_0_to_5.sql`; AD-002; AD-016
**Requirement**: AUTH-31

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Tabelas `app_user` e `email_otp` existem; `user_id` é nullable com FK
- [ ] Unique global de `rawg_id` removido; unique composto `(user_id, rawg_id)` criado
- [ ] `JpaTrackedGameRepositorySpec` não assume mais unique global em órfãos
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(auth): add Flyway V3 app_user otp and diary owner`

---

### T2: Add User domain model

**What**: Criar o record/tipo `User` no core (`id`, `email` normalizado, `createdAt`) sem Spring nem JPA.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/core/model/User.java`
**Depends on**: None
**Reuses**: `TrackedGame` record style
**Requirement**: AUTH-04

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] `User` tem id, email, createdAt
- [ ] Sem imports de Spring/JPA/Security em `core/model`
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(auth): add User domain model`

---

### T3: Add UserRepository port

**What**: Criar a porta `UserRepository` com `save` e `findByNormalizedEmail` usando só o domínio `User`.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/core/port/out/UserRepository.java`
**Depends on**: T2
**Reuses**: `TrackedGameRepository` port style
**Requirement**: AUTH-04

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Porta sem tipos Spring Data
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(auth): add UserRepository port`

---

### T4: Add UserEntity

**What**: Criar `UserEntity` mapeada para `app_user` (email unique, `created_at`).
**Where**: `src/main/java/com/brunoandreotti/game_tracker/adapter/out/persistence/UserEntity.java`
**Depends on**: T2
**Reuses**: `TrackedGameEntity` Lombok/JPA style; colunas de T1
**Requirement**: AUTH-04

**Tools**:

- MCP: `user-context7` (Spring Data JPA entity, Boot 4.1.1)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Entidade só em `adapter/out/persistence`
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(auth): add UserEntity`

---

### T5: Implement JpaUserRepository

**What**: Implementar `JpaUserRepository` (Spring Data fica no adapter) mapeando entity ↔ `User`. Specs Testcontainers: save + `findByNormalizedEmail`.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/adapter/out/persistence/JpaUserRepository.java`
**Depends on**: T1, T3, T4
**Reuses**: `JpaTrackedGameRepository`; `PostgresIntegrationSpec`
**Requirement**: AUTH-04, AUTH-05

**Tools**:

- MCP: `user-context7` (Spring Data JPA derived queries, Boot 4.1.1)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Spec `Given a normalized email, When the user is saved and loaded, Then fields match`
- [ ] Spec `Given no user for an email, When findByNormalizedEmail is called, Then empty is returned`
- [ ] Core não importa Spring Data
- [ ] Gate check passes: `mvn test`
- [ ] Test count: at least 2 tests pass (no silent deletions)

**Tests**: integration
**Gate**: full

**Commit**: `feat(auth): add JpaUserRepository`

---

### T6: Implement UserService findOrCreate

**What**: Criar `UserService` + `UserServiceImpl.findOrCreateByEmail` (trim + lowercase). Specs Spock sem Spring: cria na primeira vez; reusa o mesmo id na segunda.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/application/UserServiceImpl.java`
**Depends on**: T3
**Reuses**: Porta T3; fake in-memory no spec
**Requirement**: AUTH-04, AUTH-05, AUTH-20, AUTH-21

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Spec `Given no user for an email, When findOrCreateByEmail is called, Then a user is saved`
- [ ] Spec `Given a user already exists for that email, When findOrCreateByEmail is called, Then the existing user is returned`
- [ ] Spec `Given mixed-case email, When findOrCreateByEmail is called, Then lookup uses trim and lowercase`
- [ ] Sem Spring no spec
- [ ] Gate check passes: `mvn test -Dtest=UserServiceImplSpec`
- [ ] Test count: at least 3 tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick

**Commit**: `feat(auth): add UserService findOrCreate`

---

### Phase 2: OTP core and mail

### T7: Add EmailOtpChallenge model

**What**: Criar o record de desafio OTP no core (`email`, `codeHash`, `salt`, `expiresAt`, `failureCount`, `requestedAt`) sem Spring.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/core/model/EmailOtpChallenge.java`
**Depends on**: None
**Reuses**: Estilo de `User`
**Requirement**: AUTH-09, AUTH-14

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Campos batem com o design (hash + salt, nunca o código em claro)
- [ ] Sem Spring/JPA no core
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(auth): add EmailOtpChallenge domain`

---

### T8: Add EmailOtpRepository port

**What**: Criar `EmailOtpRepository` com `save`, `findByEmail`, `deleteByEmail`.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/core/port/out/EmailOtpRepository.java`
**Depends on**: T7
**Reuses**: `UserRepository` port style
**Requirement**: AUTH-11

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Porta sem tipos Spring Data
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(auth): add EmailOtpRepository port`

---

### T9: Add EmailOtpEntity

**What**: Criar `EmailOtpEntity` mapeada para `email_otp` (PK email, hash, salt, expires, failures, requested_at).
**Where**: `src/main/java/com/brunoandreotti/game_tracker/adapter/out/persistence/EmailOtpEntity.java`
**Depends on**: T7
**Reuses**: Colunas de T1; `UserEntity` style
**Requirement**: AUTH-14

**Tools**:

- MCP: `user-context7` (JPA entity, Boot 4.1.1)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Entidade só no adapter de persistência
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(auth): add EmailOtpEntity`

---

### T10: Implement JpaEmailOtpRepository

**What**: Implementar `JpaEmailOtpRepository`. Specs Testcontainers: save/find/delete; hash persistido, código em claro ausente.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/adapter/out/persistence/JpaEmailOtpRepository.java`
**Depends on**: T8, T9
**Reuses**: `JpaUserRepository`; `PostgresIntegrationSpec`
**Requirement**: AUTH-11, AUTH-14

**Tools**:

- MCP: `user-context7` (Spring Data JPA, Boot 4.1.1)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Spec `Given a challenge is saved, When findByEmail is called, Then hash and expiry match and no plaintext code is stored`
- [ ] Spec `Given a saved challenge, When deleteByEmail is called, Then findByEmail is empty`
- [ ] Gate check passes: `mvn test`
- [ ] Test count: at least 2 tests pass (no silent deletions)

**Tests**: integration
**Gate**: full

**Commit**: `feat(auth): add JpaEmailOtpRepository`

---

### T11: Add MailPort and JavaMailSender adapter

**What**: Criar `MailPort.sendOtp` e `JavaMailSenderMailAdapter` (texto simples, `app.mail.from`). Spec unitário com `JavaMailSender` mock: sucesso e falha → `MailDeliveryException`. YAML `spring.mail.*` e serviço Mailpit no Compose entram neste task (companions).
**Where**: `src/main/java/com/brunoandreotti/game_tracker/adapter/out/mail/JavaMailSenderMailAdapter.java`
**Depends on**: None
**Reuses**: `compose.yaml` Postgres; TimeConfig não é necessário aqui
**Requirement**: AUTH-01, AUTH-13

**Tools**:

- MCP: `user-context7` (Boot 4.1.1 `JavaMailSender`, `spring-boot-starter-mail` artifact name)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] `MailPort` no core; adapter é o único que fala SMTP
- [ ] Spec `Given JavaMailSender succeeds, When sendOtp is called, Then a message is sent to that email`
- [ ] Spec `Given JavaMailSender throws, When sendOtp is called, Then MailDeliveryException is thrown`
- [ ] Mailpit no Compose (porta 1025); `spring.mail.host` apontando para Mailpit no YAML
- [ ] Gate check passes: `mvn test -Dtest=JavaMailSenderMailAdapterSpec`
- [ ] Test count: at least 2 tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick

**Commit**: `feat(auth): add MailPort and Mailpit adapter`

---

### T12: Implement OtpAuthService request and verify

**What**: Criar `OtpAuthService` + `OtpAuthServiceImpl` (hash SHA-256+salt, TTL 10 min, 5 falhas, reenvio, 429/60 s, 503 se mail falhar). Exceções de domínio no mesmo task. Specs Spock 1:1 nos ACs de OTP (sem Spring).
**Where**: `src/main/java/com/brunoandreotti/game_tracker/application/OtpAuthServiceImpl.java`
**Depends on**: T6, T8, T11
**Reuses**: T6 `UserService`; T8 porta; `Clock` de `TimeConfig` injetado no impl; fakes no spec
**Requirement**: AUTH-01, AUTH-04, AUTH-05, AUTH-07, AUTH-08, AUTH-09, AUTH-10, AUTH-11, AUTH-12, AUTH-13, AUTH-14

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Spec `Given a valid email, When requestOtp is called, Then MailPort receives a 6-digit code and a challenge is saved`
- [ ] Spec `Given an invalid email, When requestOtp is called, Then InvalidEmailException is thrown and MailPort is not called`
- [ ] Spec `Given a successful request 30 seconds ago, When requestOtp is called again, Then OtpRateLimitedException is thrown`
- [ ] Spec `Given an unused OTP, When requestOtp is called after 60 seconds, Then the previous challenge is replaced`
- [ ] Spec `Given MailPort fails, When requestOtp is called, Then MailDeliveryException is thrown and the challenge is deleted`
- [ ] Spec `Given a matching unexpired code, When verifyOtp is called for a new email, Then findOrCreateByEmail is used`
- [ ] Spec `Given an existing user, When verifyOtp succeeds, Then no second user is created`
- [ ] Spec `Given a wrong expired or non-6-digit code, When verifyOtp is called, Then InvalidOtpException is thrown`
- [ ] Spec `Given 5 incorrect codes, When verifyOtp is called again, Then the OTP is invalidated and verify still fails`
- [ ] Spec `Given 10 minutes have passed, When verifyOtp is called, Then InvalidOtpException is thrown`
- [ ] Spec `Given a valid code for a different email, When verifyOtp is called, Then InvalidOtpException is thrown`
- [ ] Código nunca persistido em claro
- [ ] Gate check passes: `mvn test -Dtest=OtpAuthServiceImplSpec`
- [ ] Test count: at least 11 tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick

**Commit**: `feat(auth): implement OTP request and verify use case`

---

### Phase 3: Security wiring

### T13: Add security mail and OAuth2 starters plus YAML

**What**: Adicionar `spring-boot-starter-security`, `spring-boot-starter-security-oauth2-client` e starter mail (nome confirmado no Context7 para Boot 4.1.1). YAML: sessão cookie 7d HttpOnly Lax, `spring.security.oauth2.client.registration.google`, `spring.mail.*`, `app.frontend.base-url`, `app.mail.from`. Secrets só via env; atualizar `.env.example`.
**Where**: `pom.xml`
**Depends on**: None
**Reuses**: Parent Boot 4.1.1; AD-015; AD-017
**Requirement**: AUTH-17, AUTH-19

**Tools**:

- MCP: `user-context7` (Spring Boot 4.1.1 starters security, oauth2-client, mail; servlet session cookie)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Starters no `pom.xml`; YAML de sessão/OAuth2/mail preenchido
- [ ] `.env.example` documenta `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` (sem secrets)
- [ ] `mvn test` ainda verde (SecurityConfig ainda não fecha `/games`)
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `build(auth): add Security OAuth2 mail starters and session YAML`

---

### T14: Add ApiAuthenticationEntryPoint

**What**: Criar `ApiAuthenticationEntryPoint` que devolve 401 `{ status, error, message }` (mesmo contrato de `ApiErrorResponse`).
**Where**: `src/main/java/com/brunoandreotti/game_tracker/config/ApiAuthenticationEntryPoint.java`
**Depends on**: T13
**Reuses**: `ApiErrorResponse`
**Requirement**: AUTH-24, AUTH-25

**Tools**:

- MCP: `user-context7` (Spring Security 7.0 `AuthenticationEntryPoint` servlet)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] `commence` escreve JSON 401, não só status
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(auth): add JSON authentication entry point`

---

### T15: Add GoogleAuthSuccessHandler

**What**: Criar `GoogleAuthSuccessHandler`: e-mail OIDC → `UserService.findOrCreateByEmail` → mesma sessão; sem e-mail → falha sem sessão. Redirect `app.frontend.base-url`. Spec unitário com principal mock (sem Spring).
**Where**: `src/main/java/com/brunoandreotti/game_tracker/config/GoogleAuthSuccessHandler.java`
**Depends on**: T6, T13
**Reuses**: T6 `UserService`; AD-017
**Requirement**: AUTH-19, AUTH-20, AUTH-21, AUTH-23

**Tools**:

- MCP: `user-context7` (Security 7.0 `AuthenticationSuccessHandler`, OIDC email claim)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Spec `Given an OIDC principal with email, When onAuthenticationSuccess runs, Then findOrCreateByEmail is called`
- [ ] Spec `Given Google provides no email, When onAuthenticationSuccess runs, Then no user is created and no session is established`
- [ ] Spec `Given an email that already exists, When Google success runs, Then findOrCreateByEmail returns that user`
- [ ] Getter OIDC confirmado no javadoc Security 7 (não inventar)
- [ ] Gate check passes: `mvn test -Dtest=GoogleAuthSuccessHandlerSpec`
- [ ] Test count: at least 3 tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick

**Commit**: `feat(auth): add Google OAuth success handler`

---

### T16: Add GoogleAuthFailureHandler

**What**: Criar `GoogleAuthFailureHandler` que redireciona para `{frontend}/login?error=google` sem estabelecer sessão.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/config/GoogleAuthFailureHandler.java`
**Depends on**: T13
**Reuses**: `app.frontend.base-url`
**Requirement**: AUTH-22

**Tools**:

- MCP: `user-context7` (Security 7.0 `AuthenticationFailureHandler`)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Spec `Given Google sign-in is cancelled or fails, When onAuthenticationFailure runs, Then the redirect goes to login with error=google and no session is established`
- [ ] Gate check passes: `mvn test -Dtest=GoogleAuthFailureHandlerSpec`
- [ ] Test count: at least 1 test passes (no silent deletions)

**Tests**: unit
**Gate**: quick

**Commit**: `feat(auth): add Google OAuth failure handler`

---

### T17: Add SecurityFilterChain cookie CSRF and oauth2Login

**What**: Criar `SecurityConfig`: `csrf.spa()`, CORS no chain (`allowCredentials`), sessão servlet, `oauth2Login` com handlers T15/T16, `logoutUrl("/auth/logout")` 204 idempotente, `permitAll` em `/auth/otp/**`, OAuth paths e (temporariamente) `/games/**` + `/tracked-games/**` para o suite atual continuar verde. `/auth/me` authenticated. Sem `formLogin`/`httpBasic`. Entry point T14.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/config/SecurityConfig.java`
**Depends on**: T14, T15, T16
**Reuses**: `CorsProperties`; AD-015; AD-017
**Requirement**: AUTH-15, AUTH-16, AUTH-17, AUTH-19

**Tools**:

- MCP: `user-context7` (Security 7.0 servlet `HttpSecurity`, `csrf.spa()`, `oauth2Login`, logout 204)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Filter chain documentado no design está no código
- [ ] `mvn test` existente continua verde (diary/search ainda permitAll)
- [ ] Gate check passes: `mvn test`

**Tests**: none
**Gate**: full

**Commit**: `feat(auth): add SecurityFilterChain with session CSRF and Google`

---

### T18: Enable CORS credentials and CSRF headers

**What**: Atualizar `CorsConfig` com `allowCredentials(true)`, headers CSRF (`X-XSRF-TOKEN` / `XSRF-TOKEN`) e origins explícitas (nunca `*`).
**Where**: `src/main/java/com/brunoandreotti/game_tracker/config/CorsConfig.java`
**Depends on**: T13
**Reuses**: `CorsProperties`; AD-011; AD-015
**Requirement**: AUTH-03

**Tools**:

- MCP: `user-context7` (Spring CORS credentials, Security 7 CSRF cookie header names)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Credentials + headers CSRF registrados
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(auth): allow CORS credentials and CSRF headers`

---

### Phase 4: Auth HTTP and lock-down

### T19: Add AuthController OTP me and logout contract

**What**: Criar `AuthController` (`POST /auth/otp/request` 204, `POST /auth/otp/verify` 200 `{ email }` + `SecurityContext` na sessão + `establishedAt`, `GET /auth/me` 200). DTOs e mappings 400/401/429/503 no `ApiExceptionHandler` no mesmo task. `RecordingMailAdapter` no teste (não fakeia diário). Logout via Security (já em T17). Specs `@SpringBootTest` + Postgres + WireMock RAWG.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/adapter/in/web/AuthController.java`
**Depends on**: T12, T17
**Reuses**: T12 `OtpAuthService`; `RawgMockMvcIntegrationSpec`; `HttpSessionSecurityContextRepository` (Security 7)
**Requirement**: AUTH-01, AUTH-02, AUTH-03, AUTH-06, AUTH-07, AUTH-08, AUTH-12, AUTH-13, AUTH-15, AUTH-16, AUTH-17, AUTH-18, AUTH-25

**Tools**:

- MCP: `user-context7` (Security 7 servlet programmatic login, Boot 4.1.1 `@SpringBootTest`)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Spec `Given a valid email, When POST /auth/otp/request, Then 204 and the code is absent from the body`
- [ ] Spec `Given a matching code, When POST /auth/otp/verify, Then 200 with email and GET /auth/me returns that email`
- [ ] Spec `Given no session, When GET /auth/me, Then 401 with status error message`
- [ ] Spec `Given an invalid email, When POST /auth/otp/request, Then 400 JSON and no mail`
- [ ] Spec `Given a wrong code, When POST /auth/otp/verify, Then 401 and no session`
- [ ] Spec `Given a request within 60 seconds, When POST /auth/otp/request again, Then 429 JSON`
- [ ] Spec `Given mail fails, When POST /auth/otp/request, Then 503 JSON`
- [ ] Spec `Given a session, When POST /auth/logout, Then 204 and GET /auth/me is 401`
- [ ] Spec `Given no session, When POST /auth/logout, Then 204`
- [ ] Spec `Given establishedAt older than 7 days, When a protected auth request is made, Then 401`
- [ ] CSRF header nos POSTs de teste se `csrf.spa()` exigir
- [ ] Gate check passes: `mvn test`
- [ ] Test count: at least 10 tests pass (no silent deletions)

**Tests**: component
**Gate**: full

**Commit**: `feat(auth): add OTP and me HTTP endpoints`

---

### T20: Protect search and diary and update HTTP specs

**What**: Em `SecurityConfig`, exigir autenticação em `/games/**` e `/tracked-games/**`. Atualizar `GameSearchControllerSpec`, `TrackedGameControllerSpec` e `SessionControllerSpec` para login OTP real (`RecordingMailAdapter`) no `setup`. Anônimo → 401 JSON (AUTH-24).
**Where**: `src/main/java/com/brunoandreotti/game_tracker/config/SecurityConfig.java`
**Depends on**: T19
**Reuses**: T19 login + CSRF helper; AD-004
**Requirement**: AUTH-24

**Tools**:

- MCP: `user-context7` (Security 7 `authorizeHttpRequests`)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Spec existente de busca/diário/sessão passam autenticados
- [ ] Spec `Given no session, When GET /games/search or GET /tracked-games, Then 401 JSON`
- [ ] Sem `@WebMvcTest`; RAWG continua WireMock
- [ ] Gate check passes: `mvn test`
- [ ] Test count: existing HTTP specs remain (no silent deletions) plus at least 1 anonymous 401

**Tests**: component
**Gate**: full

**Commit**: `feat(auth): require session on search and diary APIs`

---

### Phase 5: Diary ownership

### T21: Add userId to TrackedGame domain

**What**: Acrescentar `userId` (`Long`, nullable) em `TrackedGame` e atualizar call sites com `null` para o compile continuar verde até os serviços ganharem dono.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/core/model/TrackedGame.java`
**Depends on**: None
**Reuses**: Record atual; AD-016
**Requirement**: AUTH-26, AUTH-31

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] `TrackedGame` inclui `userId`
- [ ] Suite compila
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(auth): add userId to TrackedGame domain`

---

### T22: Map userId on TrackedGameEntity

**What**: Mapear `userId` na entidade, remover `unique = true` de `rawg_id`, unique composto `(userId, rawgId)`.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/adapter/out/persistence/TrackedGameEntity.java`
**Depends on**: T21
**Reuses**: Flyway V3; AD-016
**Requirement**: AUTH-31

**Tools**:

- MCP: `user-context7` (JPA uniqueConstraints)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Unique global de coluna removido; unique composto na entidade
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(auth): map tracked_game user_id and per-user unique`

---

### T23: Scope TrackedGameRepository by user

**What**: Acrescentar na porta (sem remover os métodos atuais) `existsByUserIdAndRawgId`, `findAllByUserIdOrderByIdAsc`, `findByIdAndUserId` para o compile permanecer verde até T24/T25.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/core/port/out/TrackedGameRepository.java`
**Depends on**: T21
**Reuses**: Porta atual
**Requirement**: AUTH-26, AUTH-27, AUTH-28, AUTH-29

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Novos métodos por `userId` na porta; métodos antigos ainda existem
- [ ] `JpaTrackedGameRepository` ganha stubs que compilam (implementação real no T24)
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(auth): scope TrackedGameRepository by userId`

---

### T24: Implement per-user JpaTrackedGameRepository

**What**: Atualizar `JpaTrackedGameRepository` + `JpaTrackedGameRepositorySpec`: unique por usuário; dois users com o mesmo `rawgId`; órfãos (`user_id` null) não entram em `findAllByUserId`.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/adapter/out/persistence/JpaTrackedGameRepository.java`
**Depends on**: T22, T23
**Reuses**: T5 para inserir `app_user`; Testcontainers
**Requirement**: AUTH-27, AUTH-30, AUTH-31

**Tools**:

- MCP: `user-context7` (Spring Data query methods)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Spec `Given two users, When both save the same rawgId, Then both rows persist`
- [ ] Spec `Given the same user and rawgId, When a second save is attempted, Then uniqueness fails`
- [ ] Spec `Given orphan rows with null user_id, When findAllByUserId is called, Then they are not returned`
- [ ] Gate check passes: `mvn test`
- [ ] Test count: at least 3 tests pass (no silent deletions); existing JPA specs updated not deleted

**Tests**: integration
**Gate**: full

**Commit**: `feat(auth): persist tracked games per user`

---

### T25: Scope TrackedGameServiceImpl by userId

**What**: Todos os métodos de `TrackedGameService` / `TrackedGameServiceImpl` recebem `userId`. 409 só no próprio `rawgId`; id de outro user → `TrackedGameNotFoundException`. Lista vazia para user novo mesmo com órfãos no banco. Atualizar `TrackedGameServiceImplSpec`.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/application/TrackedGameServiceImpl.java`
**Depends on**: T23
**Reuses**: Specs unitários atuais; `DuplicateRawgIdException`
**Requirement**: AUTH-26, AUTH-27, AUTH-28, AUTH-29, AUTH-30

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Spec `Given user A tracks rawgId X, When user B lists, Then the list is empty`
- [ ] Spec `Given user A tracks X, When user B adds X, Then it is stored and not a conflict`
- [ ] Spec `Given user A already tracks X, When user A adds X again, Then DuplicateRawgIdException is thrown`
- [ ] Spec `Given a game owned by B, When A gets patches or deletes that id, Then TrackedGameNotFoundException is thrown`
- [ ] Spec `Given orphan rows exist, When a new user lists, Then the list is empty`
- [ ] Specs antigos atualizados com `userId` (não apagados)
- [ ] Gate check passes: `mvn test -Dtest=TrackedGameServiceImplSpec`
- [ ] Test count: at least 5 new isolation tests plus existing add/patch/delete cases

**Tests**: unit
**Gate**: quick

**Commit**: `feat(auth): isolate TrackedGameService by userId`

---

### T26: Scope SessionServiceImpl by userId

**What**: `SessionService` / `SessionServiceImpl` ganham `userId`; GET/DELETE de sessão em jogo de outro user → 404 de domínio. Atualizar `SessionServiceImplSpec`.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/application/SessionServiceImpl.java`
**Depends on**: T23
**Reuses**: `TrackedGameNotFoundException` / `PlaySessionNotFoundException`
**Requirement**: AUTH-29

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Spec `Given a session under another user game, When list or delete is called, Then a not-found exception is thrown`
- [ ] Specs felizes existentes passam com `userId` do dono
- [ ] Gate check passes: `mvn test -Dtest=SessionServiceImplSpec`
- [ ] Test count: at least 1 isolation test plus existing session cases

**Tests**: unit
**Gate**: quick

**Commit**: `feat(auth): isolate SessionService by userId`

---

### T27: Extract session user in TrackedGameController

**What**: `TrackedGameController` lê `User` do `SecurityContext` e passa `userId`. Specs HTTP: lista só do dono; 409 no próprio; 404 no id alheio; 401 anônimo já coberto em T20.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/adapter/in/web/TrackedGameController.java`
**Depends on**: T24, T25
**Reuses**: T20 login helper; JSON library-v1 inalterado
**Requirement**: AUTH-26, AUTH-27, AUTH-28, AUTH-29, AUTH-30

**Tools**:

- MCP: `user-context7` (Security 7 `SecurityContext` servlet, `@SpringBootTest`)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Spec `Given user A tracked a game, When user B lists, Then 200 and an empty array`
- [ ] Spec `Given A tracks X, When B posts the same rawgId, Then 201`
- [ ] Spec `Given A tracks X, When A posts X again, Then 409 JSON`
- [ ] Spec `Given B owns id N, When A GET PATCH or DELETE N, Then 404 JSON`
- [ ] Spec `Given first login, When GET /tracked-games, Then [] even if orphans exist`
- [ ] Gate check passes: `mvn test`
- [ ] Test count: existing controller cases remain plus at least 5 isolation cases

**Tests**: component
**Gate**: full

**Commit**: `feat(auth): bind tracked-games HTTP to session user`

---

### T28: Extract session user in SessionController

**What**: `SessionController` passa `userId`; spec HTTP 404 se o tracked game não for do usuário.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/adapter/in/web/SessionController.java`
**Depends on**: T24, T26
**Reuses**: T20 login helper; `SessionControllerSpec`
**Requirement**: AUTH-29

**Tools**:

- MCP: `user-context7` (Security 7 principal, `@SpringBootTest`)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Spec `Given a game owned by B, When A posts or deletes a session under that id, Then 404 JSON`
- [ ] Specs felizes de sessão passam com login do dono
- [ ] Gate check passes: `mvn test`
- [ ] Test count: existing session HTTP cases remain plus at least 1 isolation case

**Tests**: component
**Gate**: full

**Commit**: `feat(auth): bind session HTTP to session user`

---

### Phase 6: Frontend session

### T29: Send credentials CSRF and redirect on 401

**What**: Em `apiRequest`, `credentials: 'include'`; em POST/PATCH/DELETE ler cookie `XSRF-TOKEN` e mandar `X-XSRF-TOKEN`; se status 401 e path não for `/auth/*`, navegar para `/login`. Atualizar `apiClient.test.ts`.
**Where**: `frontend/src/api/apiClient.ts`
**Depends on**: None
**Reuses**: `ApiError`; AD-011; AD-015
**Requirement**: AUTH-40, AUTH-41

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Test `Given a successful request, When apiRequest is called, Then fetch uses credentials include`
- [ ] Test `Given POST, When apiRequest is called, Then X-XSRF-TOKEN is sent from the XSRF-TOKEN cookie`
- [ ] Test `Given 401 on /tracked-games, When apiRequest is called, Then the client navigates to /login`
- [ ] Test `Given 401 on /auth/me, When apiRequest is called, Then it does not redirect`
- [ ] Test de rede PT existente permanece
- [ ] Gate check passes: `cd frontend && npm test`
- [ ] Test count: existing apiClient tests remain plus at least 4 new cases

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ui): send session cookie CSRF and redirect on 401`

---

### T30: Add authApi client

**What**: Criar `authApi` (`requestOtp`, `verifyOtp`, `logout`, `me`) sobre `apiRequest`.
**Where**: `frontend/src/api/authApi.ts`
**Depends on**: T29
**Reuses**: `gamesApi.ts` style
**Requirement**: AUTH-01, AUTH-03, AUTH-06, AUTH-15

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Test `Given requestOtp, When called, Then POST /auth/otp/request is used`
- [ ] Test `Given verifyOtp, When called, Then POST /auth/otp/verify is used`
- [ ] Test `Given logout, When called, Then POST /auth/logout is used`
- [ ] Test `Given me, When called, Then GET /auth/me is used`
- [ ] Gate check passes: `cd frontend && npm test`
- [ ] Test count: at least 4 tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ui): add authApi`

---

### T31: Add two-step LoginPage

**What**: Página `/login` em PT: Google (`<a href>` para `{VITE_API_URL}/oauth2/authorization/google`), e-mail + enviar código, passo 2 com 6 dígitos na mesma rota. Sucesso OTP → `/`. `?error=google` → erro PT. Já autenticado → `/`. Falha OTP → erro PT e permanece. shadcn Button/Input.
**Where**: `frontend/src/pages/LoginPage.tsx`
**Depends on**: T30
**Reuses**: `ErrorMessage`; AD-012; AD-013
**Requirement**: AUTH-33, AUTH-34, AUTH-35, AUTH-36, AUTH-38, AUTH-39, AUTH-42

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Test `Given an unauthenticated user, When LoginPage renders, Then Google and email send-code controls are shown in Portuguese`
- [ ] Test `Given requestOtp succeeds, When the user sends a code, Then a 6-digit field is shown on /login`
- [ ] Test `Given verifyOtp succeeds, When the code is submitted, Then the app navigates to /`
- [ ] Test `Given verifyOtp fails, When the code is submitted, Then a Portuguese error is shown and the route stays /login`
- [ ] Test `Given error=google, When LoginPage renders, Then a Portuguese error is shown`
- [ ] Test `Given me succeeds, When LoginPage opens, Then the app navigates to /`
- [ ] Google é link top-level, não `fetch`
- [ ] Gate check passes: `cd frontend && npm test`
- [ ] Test count: at least 6 tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ui): add Portuguese two-step login page`

---

### T32: Add RequireSession route guard

**What**: Criar `RequireSession`: chama `me`; 401 → `Navigate` para `/login`; senão renderiza children. Explicar no código/comentário mínimo só se necessário — Bruno: wrapper de rota, não framework extra.
**Where**: `frontend/src/components/RequireSession.tsx`
**Depends on**: T30
**Reuses**: React Router `Navigate`; `authApi.me`
**Requirement**: AUTH-32

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Test `Given me returns 401, When RequireSession renders, Then the user is navigated to /login`
- [ ] Test `Given me succeeds, When RequireSession renders, Then children are shown`
- [ ] Gate check passes: `cd frontend && npm test`
- [ ] Test count: at least 2 tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ui): add RequireSession guard`

---

### T33: Wire login route and guarded pages

**What**: Em `App.tsx`, rota `/login` fora do `AppLayout`; `/`, `/search`, `/games/:id` dentro de `RequireSession`.
**Where**: `frontend/src/App.tsx`
**Depends on**: T31, T32
**Reuses**: Rotas atuais
**Requirement**: AUTH-32

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] `/login` sem chrome de “Meus jogos” / “Buscar”
- [ ] Rotas atuais envolvidas pelo guard
- [ ] Gate check passes: `cd frontend && npm run build`

**Tests**: none
**Gate**: build

**Commit**: `feat(ui): wire /login and session guards`

---

### T34: Add header logout control

**What**: Em `AppLayout`, mostrar logout só com sessão; click → `POST /auth/logout` e navega `/login`.
**Where**: `frontend/src/components/AppLayout.tsx`
**Depends on**: T30
**Reuses**: `AppLayout.test.tsx`; `authApi.logout`
**Requirement**: AUTH-37

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Test `Given an authenticated user, When AppLayout renders, Then a logout control is shown`
- [ ] Test `Given logout is activated, When the user clicks logout, Then logout is called and the app navigates to /login`
- [ ] Test `Given no session, When AppLayout renders, Then logout is hidden`
- [ ] Nav existente permanece
- [ ] Gate check passes: `cd frontend && npm test`
- [ ] Test count: existing nav tests remain plus at least 3 logout cases

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ui): add header logout`

---

### Phase 7: Study guide

### T35: Write backend auth study guide

**What**: Escrever só `docs/study/auth-v1-backend.md` com o backend já implementado: fluxo OTP e Google, sessão cookie/CSRF, isolamento `user_id`, mapa hexagonal (classes reais), teoria (OAuth 2 authorization code, OTP, por que não senha). Sem guia de frontend.
**Where**: `docs/study/auth-v1-backend.md`
**Depends on**: T28, T34
**Reuses**: Código mergeado deste feature; design.md só como mapa, não como fonte única
**Requirement**: study-guide success criterion

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Arquivo único no path acima
- [ ] Fluxo, motivo, classes e teoria cobertos
- [ ] Nenhum outro arquivo criado neste task

**Tests**: none
**Gate**: none

**Commit**: `docs(auth): add backend auth-v1 study guide`

---

## Phase Execution Map

```
Phase 1 -> Phase 2 -> Phase 3 -> Phase 4 -> Phase 5 -> Phase 6 -> Phase 7
```

Preferred inline order: T1 … T35.

**Batches at Execute (~7 tasks):** Phase 1 (T1–T6) | Phase 2 (T7–T12) | Phase 3 (T13–T18) | Phase 4 (T19–T20) | Phase 5 (T21–T28) | Phase 6 (T29–T34) | Phase 7 (T35). Offer sub-agents; do not auto-spawn. Phase 5 is one worker (8 tasks, tight ownership chain).

---

## Task Granularity Check

| Task | Scope | Status |
| --- | --- | --- |
| T1: Flyway V3 | 1 migration | ✅ Granular |
| T2: User model | 1 domain type | ✅ Granular |
| T3: UserRepository | 1 port | ✅ Granular |
| T4: UserEntity | 1 entity | ✅ Granular |
| T5: JpaUserRepository | 1 adapter + spec | ✅ Granular |
| T6: UserServiceImpl | 1 use case + spec | ✅ Granular |
| T7: EmailOtpChallenge | 1 domain type | ✅ Granular |
| T8: EmailOtpRepository | 1 port | ✅ Granular |
| T9: EmailOtpEntity | 1 entity | ✅ Granular |
| T10: JpaEmailOtpRepository | 1 adapter + spec | ✅ Granular |
| T11: Mail adapter | 1 adapter + spec | ✅ Granular |
| T12: OtpAuthServiceImpl | 1 use case + spec | ✅ Granular |
| T13: pom + YAML starters | 1 build file (YAML companion) | ✅ Granular |
| T14: Entry point | 1 config class | ✅ Granular |
| T15: Google success handler | 1 handler + spec | ✅ Granular |
| T16: Google failure handler | 1 handler + spec | ✅ Granular |
| T17: SecurityConfig | 1 filter chain | ✅ Granular |
| T18: CorsConfig | 1 config class | ✅ Granular |
| T19: AuthController | 1 controller + component spec | ✅ Granular |
| T20: Lock-down SecurityConfig | 1 config change + existing HTTP specs | ✅ Granular |
| T21: TrackedGame userId | 1 domain field | ✅ Granular |
| T22: TrackedGameEntity userId | 1 entity | ✅ Granular |
| T23: TrackedGameRepository port | 1 port (additive) | ✅ Granular |
| T24: JpaTrackedGameRepository | 1 adapter + spec | ✅ Granular |
| T25: TrackedGameServiceImpl | 1 service + spec | ✅ Granular |
| T26: SessionServiceImpl | 1 service + spec | ✅ Granular |
| T27: TrackedGameController | 1 controller + spec | ✅ Granular |
| T28: SessionController | 1 controller + spec | ✅ Granular |
| T29: apiClient | 1 module + tests | ✅ Granular |
| T30: authApi | 1 module + tests | ✅ Granular |
| T31: LoginPage | 1 page + tests | ✅ Granular |
| T32: RequireSession | 1 component + tests | ✅ Granular |
| T33: App routes | 1 wiring file | ✅ Granular |
| T34: AppLayout logout | 1 component + tests | ✅ Granular |
| T35: Study guide | 1 markdown file | ✅ Granular |

**Granularity check**: 1 class / 1 endpoint / 1 page per task. Companions (DTOs, handler mappings, existing spec updates) stay in the same task so the gate stays green.

---

## Diagram-Definition Cross-Check

| Task | Depends On (task body) | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | (root) | ✅ Match |
| T2 | None | (root) | ✅ Match |
| T3 | T2 | T2 -> T3 | ✅ Match |
| T4 | T2 | T2 -> T4 | ✅ Match |
| T5 | T1, T3, T4 | T1 -> T5, T3 -> T5, T4 -> T5 | ✅ Match |
| T6 | T3 | T3 -> T6 | ✅ Match |
| T7 | None | (root) | ✅ Match |
| T8 | T7 | T7 -> T8 | ✅ Match |
| T9 | T7 | T7 -> T9 | ✅ Match |
| T10 | T8, T9 | T8 -> T10, T9 -> T10 | ✅ Match |
| T11 | None | (root) | ✅ Match |
| T12 | T6 (cross-phase), T8, T11 | T8 -> T12, T11 -> T12 | ✅ Match |
| T13 | None | (root) | ✅ Match |
| T14 | T13 | T13 -> T14 | ✅ Match |
| T15 | T6 (cross-phase), T13 | T13 -> T15 | ✅ Match |
| T16 | T13 | T13 -> T16 | ✅ Match |
| T17 | T14, T15, T16 | T14 -> T17, T15 -> T17, T16 -> T17 | ✅ Match |
| T18 | T13 | T13 -> T18 | ✅ Match |
| T19 | T12, T17 (cross-phase) | (root of Phase 4) | ✅ Match |
| T20 | T19 | T19 -> T20 | ✅ Match |
| T21 | None | (root) | ✅ Match |
| T22 | T21 | T21 -> T22 | ✅ Match |
| T23 | T21 | T21 -> T23 | ✅ Match |
| T24 | T22, T23 | T22 -> T24, T23 -> T24 | ✅ Match |
| T25 | T23 | T23 -> T25 | ✅ Match |
| T26 | T23 | T23 -> T26 | ✅ Match |
| T27 | T24, T25 | T24 -> T27, T25 -> T27 | ✅ Match |
| T28 | T24, T26 | T24 -> T28, T26 -> T28 | ✅ Match |
| T29 | None | (root) | ✅ Match |
| T30 | T29 | T29 -> T30 | ✅ Match |
| T31 | T30 | T30 -> T31 | ✅ Match |
| T32 | T30 | T30 -> T32 | ✅ Match |
| T33 | T31, T32 | T31 -> T33, T32 -> T33 | ✅ Match |
| T34 | T30 | T30 -> T34 | ✅ Match |
| T35 | T28, T34 (cross-phase) | (root of Phase 7) | ✅ Match |

Cross-phase `Depends on` (T6, T12, T17, T28, T34) are satisfied when the later phase starts; they are not redrawn as intra-phase edges.

---

## Test Co-location Validation

| Task | Code Layer Created/Modified | Matrix Requires | Task Says | Status |
| --- | --- | --- | --- | --- |
| T1 | Flyway schema | none | none | ✅ OK |
| T2 | Domain type | none | none | ✅ OK |
| T3 | Port | none | none | ✅ OK |
| T4 | Entity | none | none | ✅ OK |
| T5 | Persistence adapter | integration | integration | ✅ OK |
| T6 | Domain / application | unit | unit | ✅ OK |
| T7 | Domain type | none | none | ✅ OK |
| T8 | Port | none | none | ✅ OK |
| T9 | Entity | none | none | ✅ OK |
| T10 | Persistence adapter | integration | integration | ✅ OK |
| T11 | Mail adapter (application-facing port impl) | unit | unit | ✅ OK |
| T12 | Domain / application | unit | unit | ✅ OK |
| T13 | pom / YAML config | none | none | ✅ OK |
| T14 | Security config | none | none | ✅ OK |
| T15 | Google handler | unit | unit | ✅ OK |
| T16 | Google handler | unit | unit | ✅ OK |
| T17 | SecurityConfig | none | none | ✅ OK |
| T18 | CORS config | none | none | ✅ OK |
| T19 | HTTP / component | component | component | ✅ OK |
| T20 | HTTP / component | component | component | ✅ OK |
| T21 | Domain type | none | none | ✅ OK |
| T22 | Entity | none | none | ✅ OK |
| T23 | Port | none | none | ✅ OK |
| T24 | Persistence adapter | integration | integration | ✅ OK |
| T25 | Domain / application | unit | unit | ✅ OK |
| T26 | Domain / application | unit | unit | ✅ OK |
| T27 | HTTP / component | component | component | ✅ OK |
| T28 | HTTP / component | component | component | ✅ OK |
| T29 | Front API | unit | unit | ✅ OK |
| T30 | Front API | unit | unit | ✅ OK |
| T31 | Front page | unit | unit | ✅ OK |
| T32 | Front guard | unit | unit | ✅ OK |
| T33 | Route wiring | none | none | ✅ OK |
| T34 | Front layout | unit | unit | ✅ OK |
| T35 | Study markdown | none | none | ✅ OK |

No task uses “tested in another task”. Services and controllers all carry their specs in the same task.

---

## Requirement Traceability Update

After approval, map in `spec.md`: AUTH-01…AUTH-42 → In Tasks (T1–T34). Study-guide success criterion → T35.

---

## Status

Draft — awaiting Bruno approval.
