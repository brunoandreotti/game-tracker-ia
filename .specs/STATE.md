# STATE

## Decisions

### AD-001
- **Decision**: All RAWG access goes through a `GameCatalog` port in `catalog`. HTTP client types (Feign or `@HttpExchange`) stay in the adapter. The rest of the app does not import them.
- **Reason**: Search and add both need the catalog. Swapping Feign for Boot HTTP services (or a fake in tests) must not touch tracking code.
- **Trade-off**: One extra type versus calling the HTTP client from services.
- **Scope**: `com.brunoandreotti.game_tracker.core.port.out` and every caller of catalog data
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
- **Status**: superseded (layout de pacotes — ver AD-008)

### AD-008
- **Decision**: Pacotes planos por camada técnica na raiz: `controller`, `service`, `repository`, `client`, `model`, `entity`, `dto`, `exception`, `config`. Portas hexagonais mantidas (`GameCatalogPort`, `TrackedGameRepository` + impls). Sem pastas `catalog/` ou `tracking/`.
- **Reason**: Navegação por responsabilidade (todos os controllers num lugar) é mais clara para o Bruno do que feature + domain/application/adapter.
- **Trade-off**: Perde agrupamento visual catalog vs tracking; pastas `service/` e `repository/` crescem com novas features.
- **Scope**: todo o código sob `com.brunoandreotti.game_tracker` (main + test)
- **Date**: 2026-08-27
- **Status**: superseded (ver AD-009)

### AD-009
- **Decision**: Zonas hexagonais: `core/` (model, exception, port/in, port/out), `application/` (*ServiceImpl), `adapter/in/web`, `adapter/out/persistence`, `adapter/out/rawg`, `config/`. Core sem Spring/JPA/Feign. Portas só como interfaces em `core/port`.
- **Reason**: Combinar navegação clara com papéis hexagonais visíveis (core vs port IN/OUT vs adapter).
- **Trade-off**: Mais um nível de pasta; terceiro refactor de layout em sequência.
- **Scope**: todo o código sob `com.brunoandreotti.game_tracker` (main + test); Phase 4 nasce nesta árvore
- **Date**: 2026-08-27
- **Status**: active

### AD-006
- **Decision**: RAWG HTTP via OpenFeign (`@FeignClient`), não `@HttpExchange`. Spring Cloud BOM **2025.1.x (Oakwood)** com Boot **4.1.1** — sem downgrade de Boot.
- **Reason**: Preferência explícita do Bruno; matriz oficial Spring Cloud 2025.1.2+ suporta Boot 4.1.x com release train estável.
- **Trade-off**: Dependência Spring Cloud no classpath; adapter continua isolado atrás de `GameCatalog`.
- **Scope**: `adapter/out/rawg`, `pom.xml`, `application.yaml`, testes WireMock
- **Date**: 2026-08-27
- **Status**: active

### AD-007
- **Decision**: Feature tests are Spock `*Spec.groovy`. Method names and blocks use English Given / When / Then.
- **Reason**: Specs read as scenarios. Agents keep the same shape on later work.
- **Trade-off**: Longer method names than JUnit-style sentences.
- **Scope**: `src/test/groovy/**/*Spec.groovy`
- **Date**: 2026-08-27
- **Status**: active

## Handoff

- **Feature**: library-v1 / `.specs/features/library-v1`
- **Phase / Task**: Hex zones refactor (AD-009) concluído. Phase 4 HTTP tracking (T18–T21) pendente
- **Completed**: T1–T17 (Phase 1 Infra + Phase 2 Search + Phase 3 Persistence); AD-009 layout
- **In-progress**: none
- **Next step**: Perguntar se Bruno quer Phase 4 HTTP tracking. Cadence: uma fase por vez
- **Blockers**: none
- **Uncommitted files**: refactor hex zones (src + docs)
- **Branch**: main
