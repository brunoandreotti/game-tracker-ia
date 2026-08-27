# STATE

## Decisions

### AD-001
- **Decision**: All RAWG access goes through a `GameCatalog` port in `catalog`. HTTP client types (Feign or `@HttpExchange`) stay in the adapter. The rest of the app does not import them.
- **Reason**: Search and add both need the catalog. Swapping Feign for Boot HTTP services (or a fake in tests) must not touch tracking code.
- **Trade-off**: One extra type versus calling the HTTP client from services.
- **Scope**: `com.brunoandreotti.game_tracker.catalog` and every caller of catalog data
- **Date**: 2026-08-27
- **Status**: active

### AD-002
- **Decision**: Schema is owned by Flyway (`classpath:db/migration`). `spring.jpa.hibernate.ddl-auto` is `none`. Production database is PostgreSQL via Compose.
- **Reason**: Boot 4.1 treats Hibernate as depending on Flyway when both are present. `ddl-auto` on Postgres would fight migrations. Compose + Flyway is the official path in [docs/stack.md](docs/stack.md).
- **Trade-off**: Every schema change is a SQL file. No Hibernate auto-update in dev.
- **Scope**: persistence, `compose.yaml`, `application.yaml`
- **Date**: 2026-08-27
- **Status**: active

### AD-003
- **Decision**: Persistence tests use Testcontainers PostgreSQL (`spring-boot-testcontainers`, `@ServiceConnection`). The RAWG adapter is tested against WireMock. Domain specs stay without Spring. No H2.
- **Reason**: Flyway, unique `rawg_id`, and cascade must run on Postgres. The adapter must hit a real HTTP client against a stubbed RAWG, not a mock of `GameCatalog`.
- **Trade-off**: `mvn test` needs Docker. Specs are slower than H2 or a mocked client. Domain rules stay fast in-process.
- **Scope**: test dependencies, `contextLoads`, repository specs, `RawgGameCatalog` specs, optional `@SpringBootTest` demo path
- **Date**: 2026-08-27
- **Status**: active

### AD-004
- **Decision**: Component tests start the real app (`@SpringBootTest`). Controller, service, and repository are real. Postgres is Testcontainers. Only RAWG is replaced (WireMock). Do not fake services or repositories. Do not use `@WebMvcTest` for the API contract.
- **Reason**: Component tests should match how we call the API. Faking the service only proves the controller maps JSON.
- **Trade-off**: These specs need Docker and are slower than a controller-only test.
- **Scope**: HTTP specs de busca, jogos acompanhados e sessões (T9, T19, T21)
- **Date**: 2026-08-27
- **Status**: active

### AD-005
- **Decision**: Arquitetura hexagonal prática. Controller → serviço (interface) → porta de saída (interface). Classes `*ServiceImpl` e `Jpa*Repository` / `RawgGameCatalog`. Pacotes `catalog` e `tracking` com `domain` / `application` / `adapter`. Modelo `TrackedGame`. HTTP `/tracked-games`.
- **Reason**: O miolo testa sem Spring. Banco e RAWG trocam sem mudar regra. `library` não descrevia o rastreio.
- **Trade-off**: Mais tipos (interface + classe + entidade JPA). Sem interface por endpoint.
- **Scope**: todo o código de feature sob `com.brunoandreotti.game_tracker`
- **Date**: 2026-08-27
- **Status**: active

### AD-006
- **Decision**: RAWG HTTP via OpenFeign (`@FeignClient`), não `@HttpExchange`. Spring Cloud BOM **2025.1.x (Oakwood)** com Boot **4.1.1** — sem downgrade de Boot.
- **Reason**: Preferência explícita do Bruno; matriz oficial Spring Cloud 2025.1.2+ suporta Boot 4.1.x com release train estável.
- **Trade-off**: Dependência Spring Cloud no classpath; adapter continua isolado atrás de `GameCatalog`.
- **Scope**: `catalog.adapter.http`, `pom.xml`, `application.yaml`, testes WireMock
- **Date**: 2026-08-27
- **Status**: active

## Handoff

- **Feature**: library-v1 / `.specs/features/library-v1`
- **Phase / Task**: Phase 2 Search concluída (T6–T9). Aguardando confirmação para Phase 3 Persistence (T10–T17)
- **Completed**: T1–T9 (Phase 1 Infra + Phase 2 Search)
- **In-progress**: none
- **Next step**: Perguntar se Bruno quer Phase 3 Persistence. Cadence: uma fase por vez
- **Blockers**: none
- **Uncommitted files**: `.specs/STATE.md`, `.specs/features/library-v1/context.md`, `docs/**`, `AGENTS.md`, outros `.specs/**`
- **Branch**: main
