# Game Tracker

Spring Boot backend scaffold. Product vision and planned stack live in [`docs/`](docs/README.md) ([product](docs/product.md), [stack](docs/stack.md), [open items](docs/open.md)). Implement only what is asked; do not invent beyond those docs.

## Planning

Planning is collaborative and stays open until Bruno asks to implement.

- **Ask questions** throughout planning — product, API, stack, gaps in [`docs/open.md`](docs/open.md). Do not wait for a complete spec.
- **Give opinions and suggestions** (trade-offs, defaults, what can wait). Bruno will bring ideas; help define what is still missing.
- **Propose, then confirm.** Do not write undecided behavior into `docs/` as if it were decided.
- When something is decided, update the relevant file under `docs/` and remove it from `docs/open.md`.
- Do not start coding (Java, Docker, new dependencies) until explicitly asked to implement.

## Stack

- Java 21, Spring Boot 4.1.1, Maven 3.9+ (`mvn` no PATH; wrapper `mvnw` / `mvnw.cmd` ainda no repo)
- Web: `spring-boot-starter-webmvc`
- Persistence planned: PostgreSQL driver is on the classpath; JPA/JDBC and datasource config are not added yet
- Planned stack (Compose, Feign, JPA, Testcontainers, WireMock): [`docs/stack.md`](docs/stack.md) — do not add those dependencies until implementing
- Lombok is configured as an annotation processor
- Package: `com.brunoandreotti.game_tracker`

## Commands

Use `mvn` (Maven 3.9+ and Java 21 on the PATH).

```bash
mvn test
mvn spring-boot:run
mvn -q package
```

## Layout

```
docs/                                            # product, stack, open items (planning until implemented)
src/main/java/com/brunoandreotti/game_tracker/   # application code
src/main/resources/application.yaml              # Spring config
src/test/groovy/com/brunoandreotti/game_tracker/ # Spock specs (`*Spec.groovy`)
src/test/java/com/brunoandreotti/game_tracker/   # JUnit (`contextLoads`) and shared test config
```

Keep the root package as-is. Pacotes planos por camada (AD-008): `controller`, `service`, `repository`, `client`, `model`, `entity`, `dto`, `exception`, `config`. `model` não importa Spring nem JPA.

```
src/main/java/com/brunoandreotti/game_tracker/
  controller/    # REST
  service/       # casos de uso (*Service + *ServiceImpl)
  repository/    # portas de persistência + Jpa*Repository + Spring Data interno
  client/        # GameCatalogPort + RawgGameCatalogAdapter + RawgApiClient
  model/         # domínio puro (TrackedGame, PlaySession, PlayStatus)
  entity/        # entidades JPA
  dto/           # DTOs HTTP e integração
  exception/     # exceções de negócio
  config/        # Spring config + ApiExceptionHandler
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

Use this for Spring Boot, Spring Web MVC, PostgreSQL, Lombok, Maven, JUnit, Spock, Spring Cloud OpenFeign, and any other dependency or API this project uses.

## Do not

- Invent domain entities, APIs, or product behavior beyond [`docs/product.md`](docs/product.md).
- Commit, push, or change git config unless explicitly asked.
- Rewrite the scaffold, rename the package, or add unrelated tooling.
