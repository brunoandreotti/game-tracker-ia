# Library v1 Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user - do not proceed without it.**

---

**Design**: `.specs/features/library-v1/design.md`
**Status**: Draft

---

## Test Coverage Matrix

> Generated from codebase, project guidelines, and spec - confirm before Execute. Guidelines found: `AGENTS.md`, `docs/stack.md` (unitário / integração / componente; Testcontainers; WireMock). Existing sample: `GameTrackerApplicationTests` `contextLoads` is a floor, not a ceiling.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Domain service (`GameSearchServiceImpl`, `TrackedGameServiceImpl`, `SessionServiceImpl`) | unit | Regras do spec, sem Spring | `src/test/groovy/**/*Spec.groovy` | `mvn test` |
| Catalog adapter (`RawgGameCatalog`) | integration | WireMock: mapping, 5xx/timeout → indisponível, 404 → não encontrado, year/cover nulos | `src/test/groovy/**/*Spec.groovy` | `mvn test` |
| Persistence adapter (`JpaTrackedGameRepository`, `JpaPlaySessionRepository`) | integration | Unique `rawg_id`, cascade, ordem das sessões no Postgres | `src/test/groovy/**/*Spec.groovy` | `mvn test` |
| HTTP API (`@SpringBootTest`) | component | Rotas: happy + erros 400/404/409/502. Serviço e repositório reais. Só RAWG é WireMock. Inclui o fluxo Zelda → 150 minutos | `src/test/groovy/**/*Spec.groovy` | `mvn test` |
| Port / entity / Flyway / YAML / Compose / Dockerfile | none | Build gate only | - | `mvn -q package` |

## Gate Check Commands

> Generated from codebase - confirm before Execute.

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | After tasks with unit tests only | `mvn test` |
| Full | After tasks with WireMock, Testcontainers, or `@SpringBootTest` | `mvn test` (Docker required) |
| Build | After phase completion or config/entity-only tasks | `mvn -q package` |

---

## Execution Plan

Phases are ordered and run sequentially - each phase completes before the next begins, and tasks within a phase execute in order.

### Phase 1: Infra

```
T1 -> T2 -> T5
T1 -> T3 -> T4
```

### Phase 2: Search

```
T6 -> T7 -> T9
T6 -> T8 -> T9
```

### Phase 3: Persistence

```
T10 -> T11 -> T12 -> T13 -> T14 -> T15 -> T16 -> T17
```

### Phase 4: HTTP tracking

```
T18 -> T19
T20 -> T21
```

---

## Task Breakdown

### Phase 1: Infra

### T1: Add persistence, validation, Spock, Testcontainers, and WireMock deps

**What**: Add JPA, Flyway, validation, Spock 2.4 (Groovy 4 classifier, `spock-spring`), GMavenPlus or compiler plugin for `src/test/groovy`, `spring-boot-testcontainers` + PostgreSQL Testcontainers, and WireMock (test scope). Wire `contextLoads` to a static `PostgreSQLContainer` with `@ServiceConnection`.
**Where**: `pom.xml`
**Depends on**: None
**Reuses**: Existing Boot 4.1.1 parent, Lombok processor, PostgreSQL runtime driver
**Requirement**: LIB-01

**Tools**:

- MCP: `user-context7` (Spring Boot 4.1 Testcontainers `@ServiceConnection`, Spock 2.4, Flyway, WireMock JUnit 5)
- Skill: `tlc-spec-driven`

**Done when**:

- [x] `pom.xml` has `spring-boot-starter-data-jpa`, Flyway, `spring-boot-starter-validation`, Spock, `spring-boot-testcontainers`, PostgreSQL Testcontainers, WireMock test
- [x] `GameTrackerApplicationTests` uses `@ServiceConnection` Postgres (no H2)
- [x] Gate check passes: `mvn -q package`
- [x] Existing `contextLoads` still green (no silent test deletion)

**Tests**: none
**Gate**: build

**Commit**: `build(tracking): add JPA Flyway Spock Testcontainers and WireMock`

---

### T2: Configure datasource, JPA, and Flyway for Postgres

**What**: Fill `application.yaml` with Postgres URL/user/password from env, `spring.jpa.hibernate.ddl-auto: none`, Flyway enabled, and placeholders for RAWG base URL and API key.
**Where**: `src/main/resources/application.yaml`
**Depends on**: T1
**Reuses**: Existing `spring.application.name`
**Requirement**: LIB-01

**Tools**:

- MCP: `user-context7` (Boot 4.1 datasource, JPA, Flyway properties)
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Production YAML has Postgres + `ddl-auto: none` + Flyway
- [x] No secrets committed; `RAWG_API_KEY` and DB password come from env
- [x] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `build(tracking): configure Postgres JPA and Flyway in YAML`

---

### T3: Add application Dockerfile

**What**: Root Dockerfile that packages and runs the Spring Boot app (Java 21).
**Where**: `Dockerfile`
**Depends on**: T1
**Reuses**: `pom.xml` artifact `game-tracker`
**Requirement**: LIB-01

**Tools**:

- MCP: `user-context7` (Spring Boot 4.1 container / layered jar if documented)
- Skill: `tlc-spec-driven`

**Done when**:

- [x] `Dockerfile` exists at repo root and targets Java 21
- [x] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `build(tracking): add Dockerfile for the API image`

---

### T4: Add Compose Postgres and app services

**What**: Root `compose.yaml` with PostgreSQL and the application. RAWG stays outside Compose.
**Where**: `compose.yaml`
**Depends on**: T3
**Reuses**: T3 Dockerfile; Boot 4.1 Compose Postgres example
**Requirement**: LIB-01

**Tools**:

- MCP: `user-context7` (Boot 4.1 Docker Compose Postgres)
- Skill: `tlc-spec-driven`

**Done when**:

- [x] `compose.yaml` defines Postgres + app
- [x] App service passes DB env; RAWG key via env only
- [x] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `build(tracking): add Compose for Postgres and the API`

---

### T5: Create Flyway V1 schema for tracked games and sessions

**What**: Versioned SQL `V1__tracked_games_and_sessions.sql` with `tracked_game` (unique `rawg_id`) and `play_session` (FK cascade, date + duration). Postgres dialect is fine (tests use Testcontainers, not H2).
**Where**: `src/main/resources/db/migration/V1__tracked_games_and_sessions.sql`
**Depends on**: T2
**Reuses**: Flyway default location `classpath:db/migration`; naming `V1__Description.sql`
**Requirement**: LIB-09

**Tools**:

- MCP: `user-context7` (Flyway versioned SQL naming)
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Migration creates both tables, unique `rawg_id`, FK `ON DELETE CASCADE`
- [x] `mvn test` applies the migration on Testcontainers Postgres
- [x] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(tracking): add Flyway schema for tracked games and sessions`

---

### Phase 2: Search

### T6: Define GameCatalog port and catalog exceptions

**What**: `GameCatalog` with nested `GameSummary` and exceptions `GameNotFoundException` / `CatalogUnavailableException`. Methods `search` and `getByRawgId`.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/catalog/application/GameCatalog.java`
**Depends on**: T1
**Reuses**: Package layout from `AGENTS.md` (`catalog` under the root package)
**Requirement**: LIB-01

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Port matches design: `search(String)`, `getByRawgId(long)`
- [x] Exceptions exist for 404 (missing RAWG id) and 502 (unavailable)
- [x] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(catalog): add GameCatalog port`

---

### T7: Implement RAWG adapter behind GameCatalog

**What**: HTTP client (Feign if BOM gate passes, else `@HttpExchange` group `rawg`) plus `RawgGameCatalog` implementing `GameCatalog`. Map RAWG JSON to `GameSummary`. Timeout/5xx → `CatalogUnavailableException`. Missing id → `GameNotFoundException`. Null year/cover allowed. Specs use WireMock, not a mock of the HTTP interface.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/catalog/adapter/http/RawgGameCatalog.java`
**Depends on**: T6
**Reuses**: AD-001; AD-003; Boot 4.1.0 HTTP services or OpenFeign `@FeignClient`; `application.yaml` RAWG properties from T2
**Requirement**: LIB-04

**Tools**:

- MCP: `user-context7` (OpenFeign BOM vs Boot 4.1.1, or `@ImportHttpServices` / `@GetExchange`; official RAWG API; WireMock JUnit 5)
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Adapter is the only type that talks HTTP to RAWG
- [x] WireMock specs cover mapping, 5xx/timeout → `CatalogUnavailableException`, 404 → `GameNotFoundException`, null year/cover
- [x] Gate check passes: `mvn test`
- [x] Test count: at least 4 tests pass (no silent deletions)

**Tests**: integration
**Gate**: full

**Commit**: `feat(catalog): adapt RAWG behind GameCatalog`

---

### T8: Add GameSearchService and implementation

**What**: Interface `GameSearchService` and class `GameSearchServiceImpl` that delegates to `GameCatalog`. Does not persist. Controller must not call `GameCatalog`.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/catalog/application/GameSearchService.java`
**Depends on**: T6
**Reuses**: T6 `GameCatalog`; in-memory fake in Spock (no Spring)
**Requirement**: LIB-01

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [x] `GameSearchServiceImpl` implements the interface and only depends on `GameCatalog`
- [x] Spock specs (no Spring) cover returning summaries from the port and empty list; no persistence call exists on the service
- [x] Gate check passes: `mvn test`
- [x] Test count: at least 2 tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick

**Commit**: `feat(catalog): add GameSearchService`

---

### T9: Expose GET /games/search and API error body

**What**: `GameSearchController` for `GET /games/search?q=`, Bean Validation `@NotBlank` on `q`, JSON array `{ rawgId, name, year, coverUrl }`. Inject `GameSearchService`. `ApiExceptionHandler` + error record `{ status, error, message }` for 400/404/409/502.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/catalog/adapter/web/GameSearchController.java`
**Depends on**: T5, T7, T8
**Reuses**: `@RestController`, constructor injection; handler lives in `config` (created in this task)
**Requirement**: LIB-01

**Tools**:

- MCP: `user-context7` (Web MVC `@RequestParam` validation, `@RestControllerAdvice`, `@SpringBootTest`)
- Skill: `tlc-spec-driven`

**Done when**:

- [x] `@SpringBootTest` specs (Postgres + WireMock) assert LIB-01, LIB-02 (search does not persist), LIB-03 (blank `q` → 400), LIB-04 (unavailable → 502), LIB-05 (empty list → 200 `[]`)
- [x] Controller calls `GameSearchService`; catalog adapter is real; only RAWG HTTP is WireMock
- [x] Error JSON has `status`, `error`, `message`
- [x] Gate check passes: `mvn test`
- [x] Test count: at least 5 tests pass (no silent deletions)

**Tests**: component
**Gate**: full

**Commit**: `feat(catalog): add game search endpoint`

---

### Phase 3: Persistence

### T10: Add TrackedGame domain and PlayStatus

**What**: `TrackedGame` (no JPA) and enum `PlayStatus` (`WANT_TO_PLAY`, `PLAYING`, `COMPLETED`, `DROPPED`). Domain exceptions for not found and duplicate `rawgId`.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/tracking/domain/TrackedGame.java`
**Depends on**: T5
**Reuses**: Lombok `@Getter` if it reduces noise
**Requirement**: LIB-06

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Domain type has id, rawgId, name, year, coverUrl, status, rating
- [x] No Spring or JPA imports in `tracking.domain`
- [x] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(tracking): add TrackedGame domain`

---

### T11: Add PlaySession domain

**What**: `PlaySession` domain type with id, trackedGameId, durationMinutes, playedAt (`LocalDate`).
**Where**: `src/main/java/com/brunoandreotti/game_tracker/tracking/domain/PlaySession.java`
**Depends on**: T10
**Reuses**: T10 package `tracking.domain`
**Requirement**: LIB-27

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Domain type matches the design
- [ ] No Spring or JPA imports
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(tracking): add PlaySession domain`

---

### T12: Add TrackedGameRepository port

**What**: Interface `TrackedGameRepository` in application: save, findById, findAllOrderByIdAsc, existsByRawgId, deleteById. Uses domain `TrackedGame`, not JPA entities.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/tracking/application/TrackedGameRepository.java`
**Depends on**: T11
**Reuses**: T10 `TrackedGame`
**Requirement**: LIB-14

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Port has no Spring Data types
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(tracking): add TrackedGameRepository port`

---

### T13: Add PlaySessionRepository port

**What**: Interface `PlaySessionRepository`: save, listByTrackedGameId ordered (`playedAt` desc, `id` desc), findByIdAndTrackedGameId, delete, sumDurationByTrackedGameId.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/tracking/application/PlaySessionRepository.java`
**Depends on**: T12
**Reuses**: T11 `PlaySession`
**Requirement**: LIB-31

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Port has no Spring Data types
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(tracking): add PlaySessionRepository port`

---

### T14: Add TrackedGameEntity

**What**: JPA `TrackedGameEntity` mapped to `tracked_game` with unique `rawgId`, nullable year/cover/rating, enum `PlayStatus`.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/tracking/adapter/persistence/TrackedGameEntity.java`
**Depends on**: T13
**Reuses**: Flyway columns from T5; T10 `PlayStatus`
**Requirement**: LIB-06

**Tools**:

- MCP: `user-context7` (Spring Data JPA entity, enum mapping)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Entity lives only in `adapter.persistence`
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(tracking): add TrackedGameEntity`

---

### T15: Add PlaySessionEntity and cascade

**What**: JPA `PlaySessionEntity` mapped to `play_session`. Wire `@OneToMany` cascade + orphanRemoval on `TrackedGameEntity` so deleting a tracked game removes sessions.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/tracking/adapter/persistence/PlaySessionEntity.java`
**Depends on**: T14
**Reuses**: T14 `TrackedGameEntity`; Flyway FK cascade
**Requirement**: LIB-25

**Tools**:

- MCP: `user-context7` (JPA OneToMany orphanRemoval)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Session entity maps duration and `playedAt` date
- [ ] Tracked game owns the collection with cascade delete
- [ ] Gate check passes: `mvn -q package`

**Tests**: none
**Gate**: build

**Commit**: `feat(tracking): add PlaySessionEntity with cascade`

---

### T16: Implement JpaTrackedGameRepository

**What**: `JpaTrackedGameRepository` implements `TrackedGameRepository`. Maps entity ↔ domain. Spring Data stays inside `adapter.persistence`.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/tracking/adapter/persistence/JpaTrackedGameRepository.java`
**Depends on**: T15
**Reuses**: T12 port; T14 entity; Testcontainers from T1
**Requirement**: LIB-14

**Tools**:

- MCP: `user-context7` (Spring Data JPA repository query methods; Boot 4.1 `@ServiceConnection`)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Adapter can detect duplicate `rawgId` and load by id
- [ ] Spock against Testcontainers covers unique `rawg_id` and find-by-id
- [ ] Application code does not import Spring Data
- [ ] Gate check passes: `mvn test`
- [ ] Test count: at least 2 tests pass (no silent deletions)

**Tests**: integration
**Gate**: full

**Commit**: `feat(tracking): add JpaTrackedGameRepository`

---

### T17: Implement JpaPlaySessionRepository

**What**: `JpaPlaySessionRepository` implements `PlaySessionRepository`. List ordered, ownership lookup, SUM of duration, cascade delete with the tracked game.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/tracking/adapter/persistence/JpaPlaySessionRepository.java`
**Depends on**: T16
**Reuses**: T13 port; T15 entity; Testcontainers from T1
**Requirement**: LIB-31

**Tools**:

- MCP: `user-context7` (Spring Data derived queries, OrderBy)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Adapter can list ordered sessions and load by id scoped to a tracked game
- [ ] Spock against Testcontainers covers order (`playedAt` desc, `id` desc) and cascade delete
- [ ] Gate check passes: `mvn test`
- [ ] Test count: at least 2 tests pass (no silent deletions)

**Tests**: integration
**Gate**: full

**Commit**: `feat(tracking): add JpaPlaySessionRepository`

---

### Phase 4: HTTP tracking

### T18: Implement TrackedGameService domain rules

**What**: Interface `TrackedGameService` and `TrackedGameServiceImpl` for add (snapshot from `GameCatalog`, default `PLAYING`, rating null, `totalMinutes` 0), list by id asc, get, patch (partial, free status transitions, ignore `rating: null`), delete. Duplicate → 409. Unknown RAWG id / unknown tracked game → not-found. `totalMinutes` via SUM of sessions on read.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/tracking/application/TrackedGameService.java`
**Depends on**: T6, T17
**Reuses**: `GameCatalog` (T6); ports T12/T13; in-memory fakes in Spock (no Spring)
**Requirement**: LIB-06

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Impl depends on `GameCatalog`, `TrackedGameRepository`, `PlaySessionRepository` only
- [ ] Spock domain specs cover LIB-06–LIB-13, LIB-15–LIB-26 (snapshot, default status, 409, 404 catalog, computed `totalMinutes`, free transitions, PATCH rules, delete cascade, `rating: null` no-op)
- [ ] No Spring context in these specs
- [ ] Gate check passes: `mvn test`
- [ ] Test count: at least 12 tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick

**Commit**: `feat(tracking): implement TrackedGameService`

---

### T19: Expose tracked-games HTTP API

**What**: `TrackedGameController` for POST/GET/PATCH/DELETE `/tracked-games` and GET `/tracked-games/{id}` with DTO validation. Inject `TrackedGameService`. Wire 201/200/204 and handler mappings for 400/404/409/502.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/tracking/adapter/web/TrackedGameController.java`
**Depends on**: T9, T18
**Reuses**: `ApiExceptionHandler` from T9; `@Valid` DTOs
**Requirement**: LIB-06

**Tools**:

- MCP: `user-context7` (Web MVC `@PostMapping` 201, `@PatchMapping`, `@ResponseStatus` 204, `@SpringBootTest`)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] `@SpringBootTest` (Postgres + WireMock) covers happy paths plus 400/404/409/502 for `/tracked-games` (LIB-06–LIB-26 HTTP)
- [ ] Controller, `TrackedGameServiceImpl`, and JPA adapters are real
- [ ] JSON matches product.md (including `totalMinutes`)
- [ ] Gate check passes: `mvn test`
- [ ] Test count: at least 8 tests pass (no silent deletions)

**Tests**: component
**Gate**: full

**Commit**: `feat(tracking): add tracked-games HTTP endpoints`

---

### T20: Implement SessionService domain rules

**What**: Interface `SessionService` and `SessionServiceImpl` for create (`playedAt` default today, any play status), list ordered, delete with ownership 404, `totalMinutes` = SUM after create/delete. Reject `durationMinutes` ≤ 0.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/tracking/application/SessionService.java`
**Depends on**: T17
**Reuses**: ports T12/T13; in-memory fakes in Spock
**Requirement**: LIB-27

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Impl depends on `TrackedGameRepository` and `PlaySessionRepository` only
- [ ] Spock domain specs cover LIB-27–LIB-37 (two sessions 90+60 → 150, default date, any status, delete recalc, unknown game/session 404, duration ≤ 0, duplicate same `playedAt` allowed)
- [ ] Gate check passes: `mvn test`
- [ ] Test count: at least 8 tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick

**Commit**: `feat(tracking): implement SessionService`

---

### T21: Expose session HTTP API

**What**: `SessionController` for POST/GET `/tracked-games/{id}/sessions` and DELETE `/tracked-games/{id}/sessions/{sessionId}`. Inject `SessionService`. 201/200/204. Validation 400 for duration and `playedAt` format.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/tracking/adapter/web/SessionController.java`
**Depends on**: T9, T20
**Reuses**: T9 exception handler; T19 JSON error shape
**Requirement**: LIB-27

**Tools**:

- MCP: `user-context7` (Web MVC nested resources; Boot 4.1 Testcontainers; WireMock)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] `@SpringBootTest` (Postgres + WireMock) covers LIB-27–LIB-37 HTTP (201, default `playedAt`, list order, 204 delete, 400, 404 nested)
- [ ] Controller, `SessionServiceImpl`, and JPA adapters are real
- [ ] Same style of spec covers the demo: search → track → 90+60 → `totalMinutes` 150 → PATCH 9 + COMPLETED
- [ ] Gate check passes: `mvn test`
- [ ] Test count: at least 7 tests pass (no silent deletions)

**Tests**: component
**Gate**: full

**Commit**: `feat(tracking): add session HTTP endpoints`

---

## Phase Execution Map

Visual representation of task ordering. Phases run in sequence, and tasks within a phase run in order:

```
Phase 1 -> Phase 2 -> Phase 3 -> Phase 4
```

```
T1 -> T2 -> T5
T1 -> T3 -> T4
```

```
T6 -> T7 -> T9
T6 -> T8 -> T9
```

```
T10 -> T11 -> T12 -> T13 -> T14 -> T15 -> T16 -> T17
```

```
T18 -> T19
T20 -> T21
```

Execution is strictly sequential - there is no intra-phase parallelism. A single agent (or batch worker) works one task at a time, in order.

**How phase-based execution works:**

At Execute, the agent counts total tasks and packs phases into **task-budgeted batches** (~7 tasks per worker, whole phases). This feature has **21 tasks** in 4 phases. That is more than one batch. Offer sub-agents; do not auto-spawn. Suggested packing: Phase 1 (T1–T5) as batch 1; Phase 2 (T6–T9) as batch 2; Phase 3 (T10–T17) as batch 3; Phase 4 (T18–T21) as batch 4. Or Phase 1+2 (9) / Phase 3 (8) / Phase 4 (4).

**The orchestrating agent's role during Execute:**
1. Count total tasks and pack phases into ~7-task batches - offer batch sub-agents if that yields more than one batch and the user accepts
2. Dispatch the next batch (to a worker, or execute inline)
3. Receive the compact batch summary
4. Update tasks.md with results
5. If the batch summary shows all tasks complete: proceed to the next batch
6. If a task failed: decide fix/escalate before dispatching the next batch

---

## Task Granularity Check

| Task | Scope | Status |
| --- | --- | --- |
| T1: Maven deps + Testcontainers on `contextLoads` | 1 primary file (`pom.xml`) | Granular |
| T2: application.yaml | 1 file | Granular |
| T3: Dockerfile | 1 file | Granular |
| T4: compose.yaml | 1 file | Granular |
| T5: Flyway V1 | 1 file | Granular |
| T6: GameCatalog port | 1 file | Granular |
| T7: RAWG adapter | 1 primary file | Granular |
| T8: GameSearchService | 1 primary file | Granular |
| T9: Search controller + error handler | 1 primary file (handler in `config`) | OK if cohesive |
| T10: TrackedGame domain | 1 file | Granular |
| T11: PlaySession domain | 1 file | Granular |
| T12: TrackedGameRepository port | 1 file | Granular |
| T13: PlaySessionRepository port | 1 file | Granular |
| T14: TrackedGameEntity | 1 file | Granular |
| T15: PlaySessionEntity | 1 file | Granular |
| T16: JpaTrackedGameRepository | 1 file + spec | Granular |
| T17: JpaPlaySessionRepository | 1 file + spec | Granular |
| T18: TrackedGameService | 1 primary file + specs | Granular |
| T19: TrackedGameController | 1 file + specs | Granular |
| T20: SessionService | 1 primary file + specs | Granular |
| T21: SessionController | 1 file + specs | Granular |

**Granularity check**: 1 component / 1 endpoint per task. T9 is the cohesive pair (controller + advice) so search error paths are testable in the same gate.

---

## Diagram-Definition Cross-Check

| Task | Depends On (task body) | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | (root) | Match |
| T2 | T1 | T1 -> T2 | Match |
| T3 | T1 | T1 -> T3 | Match |
| T4 | T3 | T3 -> T4 | Match |
| T5 | T2 | T2 -> T5 | Match |
| T6 | T1 (cross-phase) | no intra-phase arrow | Match |
| T7 | T6 | T6 -> T7 | Match |
| T8 | T6 | T6 -> T8 | Match |
| T9 | T5 (cross-phase), T7, T8 | T7 -> T9, T8 -> T9 | Match |
| T10 | T5 (cross-phase) | no intra-phase arrow | Match |
| T11 | T10 | T10 -> T11 | Match |
| T12 | T11 | T11 -> T12 | Match |
| T13 | T12 | T12 -> T13 | Match |
| T14 | T13 | T13 -> T14 | Match |
| T15 | T14 | T14 -> T15 | Match |
| T16 | T15 | T15 -> T16 | Match |
| T17 | T16 | T16 -> T17 | Match |
| T18 | T6 (cross-phase), T17 (cross-phase) | no intra-phase arrow | Match |
| T19 | T9 (cross-phase), T18 | T18 -> T19 | Match |
| T20 | T17 (cross-phase) | no intra-phase arrow | Match |
| T21 | T9 (cross-phase), T20 | T20 -> T21 | Match |

## Test Co-location Validation

| Task | Code Layer Created/Modified | Matrix Requires | Task Says | Status |
| --- | --- | --- | --- | --- |
| T1 | config (pom + contextLoads Testcontainers) | none | none | OK |
| T2 | YAML config | none | none | OK |
| T3 | Dockerfile | none | none | OK |
| T4 | Compose | none | none | OK |
| T5 | Flyway schema | none | none | OK |
| T6 | port | none | none | OK |
| T7 | catalog adapter | integration | integration | OK |
| T8 | domain service | unit | unit | OK |
| T9 | HTTP API | component | component | OK |
| T10 | domain type | none | none | OK |
| T11 | domain type | none | none | OK |
| T12 | port | none | none | OK |
| T13 | port | none | none | OK |
| T14 | entity | none | none | OK |
| T15 | entity | none | none | OK |
| T16 | persistence adapter | integration | integration | OK |
| T17 | persistence adapter | integration | integration | OK |
| T18 | domain service | unit | unit | OK |
| T19 | HTTP API | component | component | OK |
| T20 | domain service | unit | unit | OK |
| T21 | HTTP API + demo | component | component | OK |

Componente = `@SpringBootTest` com serviço e repositório reais (AD-004). Integração = Postgres ou WireMock no cliente RAWG (AD-003). Unitário = `*ServiceImpl` sem Spring.

---

## Task Verification Standards

Every task follows `Done when` + `Tests` + `Gate`. `Done when` is binary. Gate commands come from **Gate Check Commands**. Test counts are floors so tests are not silently dropped.
