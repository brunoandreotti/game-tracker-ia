# Game Tracker

Spring Boot API + UI planejada em `frontend/`. Product vision and planned stack live in [`docs/`](docs/README.md) ([product](docs/product.md), [stack](docs/stack.md), [open items](docs/open.md)). Implement only what is asked; do not invent beyond those docs.

## Planning

Planning is collaborative and stays open until Bruno asks to implement.

- **Ask questions** throughout planning — product, API, stack, UI, gaps in [`docs/open.md`](docs/open.md). Do not wait for a complete spec.
- **Give opinions and suggestions** (trade-offs, defaults, what can wait). Bruno will bring ideas; help define what is still missing.
- **Propose, then confirm.** Do not write undecided behavior into `docs/` as if it were decided.
- When something is decided, update the relevant file under `docs/` and remove it from `docs/open.md`.
- Do not start coding (Java, `frontend/`, Docker, new dependencies) until explicitly asked to implement.
- Front-end: Bruno knows TypeScript but little of UI frameworks — explain React concepts briefly when they appear; prefer a thin stack (see [`docs/stack.md`](docs/stack.md) and [`.specs/features/ui-v1`](.specs/features/ui-v1/spec.md)).

## Stack

- Java 21, Spring Boot 4.1.1, Maven 3.9+ (`mvn` on PATH; wrapper `mvnw` / `mvnw.cmd` still in repo)
- Web: `spring-boot-starter-webmvc`
- Persistence: PostgreSQL + JPA + Flyway (see stack.md)
- Planned UI: React + Vite + TypeScript in `frontend/` — do not scaffold until asked to implement UI v1
- Lombok is configured as an annotation processor
- Package: `com.brunoandreotti.game_tracker`

## Commands

Use `mvn` (Maven 3.9+ and Java 21 on the PATH).

```bash
mvn test
mvn spring-boot:run
mvn -q package
```

UI (após existir `frontend/`):

```bash
cd frontend && npm install && npm run dev
```

## Layout

```
docs/                                            # product, stack, open items
.specs/features/                                 # library-v1 (API), ui-v1 (UI)
frontend/                                        # React + Vite + TS (UI v1 — criar só na Execute)
src/main/java/com/brunoandreotti/game_tracker/   # API application code
src/main/resources/application.yaml              # Spring config
src/test/groovy/com/brunoandreotti/game_tracker/ # Spock specs (`*Spec.groovy`)
src/test/java/com/brunoandreotti/game_tracker/   # JUnit (`contextLoads`) and shared test config
```

Keep the root package as-is. Zonas hexagonais (AD-009). `core/` não importa Spring, JPA nem Feign. Não mover o backend para `api/` sem decisão nova (AD-010).

```
src/main/java/com/brunoandreotti/game_tracker/
  core/
    model/              domínio + GameSummary
    exception/
    port/in/            *Service (portas de entrada)
    port/out/           *Repository, GameCatalogPort (portas de saída)
  application/          *ServiceImpl (use cases)
  adapter/
    in/web/             Controllers + DTOs HTTP
    out/persistence/    Entity, Jpa*, Spring Data
    out/rawg/           RawgGameCatalogAdapter, Feign, DTOs RAWG
  config/               Spring wiring + ApiExceptionHandler
```

## Conventions

- Match existing Spring Boot style: constructor injection, `@RestController` for HTTP, YAML for config.
- Use Lombok where it reduces noise (`@RequiredArgsConstructor`, `@Getter`); do not mix it with manual boilerplate for the same members.
- Do not add dependencies (JPA, security, validation, OpenAPI, etc.) unless the task needs them.
- Prefer small, focused classes over large service/controller files.
- Tests live next to the same package as production code. Feature tests are Spock `*Spec.groovy` in `src/test/groovy`. Keep `contextLoads` green in JUnit. Add focused specs for new behavior.
- **Spock feature methods** (English, every new or changed spec):
  - `def` name: `Given ..., When ..., Then ...` (capital G/W/T, comma-separated).
  - Blocks: `given:`, `when:`, `then:` each with an English description string. Prefer `when`/`then` over `expect`.
  - Do not use sentence-style names without Given/When/Then. Match existing specs.
- Configuration belongs in `application.yaml` (and profile-specific YAML when needed), not hardcoded in Java.
- **Class suffixes** (every class must indicate its role):
  - `Controller` — REST adapter
  - `Service` / `ServiceImpl` — application use case (interface + implementation)
  - `Port` — outbound interface (hexagonal)
  - `Adapter` — port implementation (e.g. RAWG, JPA)
  - `Repository` / `RepositoryImpl` — persistence port + JPA adapter
  - `Client` — HTTP/Feign client
  - `Dto` — records/DTOs between layers or external APIs
  - `Response` — HTTP response body (web layer)
  - `Entity` — JPA entity
  - `Exception` / `Handler` — errors
  - `Config` / `Properties` — Spring configuration

## Documentation

Always use the Context7 MCP when looking up library, framework, or API docs. Do not rely on training data or generic web search for that.
Always use the Context7 MCP when I need code generation, setup or configuration step to be sure that the implementation is right.

1. Call `resolve-library-id` with the library name and the full question as `query`.
2. Pick the best match (exact name, official source, version-specific ID when a version is mentioned — e.g. Spring Boot 4.1.1, Java 21).
3. Call `query-docs` with that `libraryId` and the specific question.
4. Answer from the fetched docs; cite the library version when relevant.

Use this for Spring Boot, Spring Web MVC, PostgreSQL, Lombok, Maven, JUnit, Spock, Spring Cloud OpenFeign, React, Vite, and any other dependency or API this project uses.

## Do not

- Invent domain entities, APIs, or product behavior beyond [`docs/product.md`](docs/product.md).
- Commit, push, or change git config unless explicitly asked.
- Rewrite the scaffold, rename the package, or add unrelated tooling.
- Scaffold `frontend/` or add UI deps until explicitly asked to implement UI v1.
