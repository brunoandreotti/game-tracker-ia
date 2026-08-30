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

### AD-010
- **Decision**: UI v1 lives in `frontend/` at the repo root (React + Vite + TypeScript SPA). Backend stays at the repo root; do not move Java to `api/` in this step. UI Portuguese, diary-minimal, routes `/`, `/search`, `/games/:id`.
- **Reason**: Guided planning 2026-08-28 — less churn than a full `api/`+`frontend/` split; happy-path UI needs search + track + list + detail + sessions.
- **Trade-off**: Root mixes Maven and (later) npm; a future `api/` move remains possible.
- **Scope**: `frontend/`, CORS/`VITE_API_URL` on Execute, docs + `.specs/features/ui-v1`
- **Date**: 2026-08-28
- **Status**: active

### AD-011
- **Decision**: UI talks to the API with a thin `fetch` client (`apiClient` + `gamesApi`). React Router in library mode (`BrowserRouter`). No Redux/TanStack Query in v1. CORS via Spring `WebMvcConfigurer` and `app.cors.allowed-origins` (default `http://localhost:5173`). Front tests: Vitest + Testing Library (no E2E in v1).
- **Reason**: Fewer moving parts while Bruno learns React; CORS is the real cross-origin contract for a separate Vite port.
- **Trade-off**: More manual loading/error state per page; no shared server-cache layer.
- **Scope**: `frontend/src/api`, `CorsConfig`, ui-v1 design/tasks
- **Date**: 2026-08-28
- **Status**: active

### AD-012
- **Decision**: UI dark-only no v1, inspirada em Letterboxd/Backloggd: canvas `#12161a`, accent verde `#00c030`, tipografia Fraunces + DM Sans, marca “Game Tracker”. Detalhe usa painéis `log-panel` alinhados para progresso e sessões.
- **Reason**: UAT visual — UI clara parecia scaffold; Bruno pediu tema escuro de diário e formulários de log mais alinhados.
- **Trade-off**: Sem toggle claro no v1; fontes via Google Fonts (rede no primeiro load).
- **Scope**: `frontend/` CSS + AppLayout + TrackedGameDetailPage
- **Date**: 2026-08-28
- **Status**: active

### AD-013
- **Decision**: Adotar **shadcn/ui + Tailwind** no `frontend/` (estudo). Componentes via CLI copiados para `src/components/ui/`. Não usar antd/MUI/Mantine neste passo.
- **Reason**: Bruno quer framework de componentes para aprender; shadcn mantém código no repo e encaixa no dark Letterboxd (AD-012) melhor que kits enterprise.
- **Trade-off**: Introduz Tailwind (reversão da linha “sem Tailwind no v1”); setup inicial maior; migração gradual do CSS global.
- **Scope**: `frontend/` — init Tailwind/shadcn; primeiro uso no detalhe (Select, Dialog, Card, Button, Input)
- **Date**: 2026-08-28
- **Status**: active

### AD-014
- **Decision**: Nota do diário é inteiro **0–5** (opcional: `null` = sem nota). UI: Select numérico no detalhe; **estrelas só na listagem** `/`. Migração: valores > 5 clampam para 5. `PATCH` com `rating: null` continua no-op (sem desavaliar).
- **Reason**: Bruno pediu escala curta e estrelas na lista; confirmou recomendação C + clamp (2026-08-28).
- **Trade-off**: Quebra escala 1–10 (library-v1 / demos com nota 9); `0` ≠ `null` (zero é nota válida).
- **Scope**: API validation + Flyway, `docs/product.md` / `docs/stack.md`, detalhe Select, lista `/`, specs `rating-0-5`
- **Date**: 2026-08-28
- **Status**: active

### AD-015
- **Decision**: Sessão autenticada é cookie de `HttpSession` servlet (HttpOnly, `SameSite=Lax`, 7 dias ou logout), não Bearer JWT. A SPA em `localhost:5173` envia o cookie para a API em `localhost:8080` com CORS `credentials`.
- **Reason**: Spring Security `oauth2Login` (Google) já persiste sessão. OTP grava o mesmo `SecurityContext`. F5 reenvia o cookie sem `localStorage`. Um mecanismo só para os dois logins.
- **Trade-off**: CORS e CSRF ficam mais rígidos que um header `Authorization`. JWT no front seria mais simples no `fetch`, mas duplicaria o fluxo Google e exporia o token ao JS.
- **Scope**: `config/SecurityConfig`, `AuthController`, `frontend/src/api/apiClient.ts`, `CorsConfig`
- **Date**: 2026-08-30
- **Status**: active

### AD-016
- **Decision**: Jogos acompanhados pertencem a um `user_id`. Unique passa a ser `(user_id, rawg_id)`. Linhas pré-auth não são atribuídas a ninguém (`user_id` null) e a API nunca as devolve.
- **Reason**: Sem dono, login seria só um portão na frente de um diário compartilhado. Spec de isolamento (404 no id alheio, 409 só no próprio `rawgId`).
- **Trade-off**: Schema e todos os serviços de diário mudam. Dados locais atuais ficam órfãos (estudo; sem migração).
- **Scope**: Flyway V3, `TrackedGame` / `TrackedGameEntity` / `TrackedGameRepository`, `TrackedGameService`, `SessionService`, specs HTTP de diário
- **Date**: 2026-08-30
- **Status**: active

### AD-017
- **Decision**: Autenticação e sessão usam Spring Security servlet (`SecurityFilterChain`, `oauth2Login`, logout), não filtros HTTP caseiros nem um Authorization Server próprio.
- **Reason**: Paths e sessão Google estão documentados no Security 7. OTP só precisa gravar o contexto na mesma sessão. Core continua sem tipos de Security (AD-009).
- **Trade-off**: Superfície maior (CSRF, CORS no filter chain, starters novos) contra um `Filter` mínimo. Redis / Spring Authorization Server continuam fora.
- **Scope**: `config/`, `pom.xml` (`spring-boot-starter-security`, `spring-boot-starter-security-oauth2-client`), adapter web de auth
- **Date**: 2026-08-30
- **Status**: active

## Handoff

- **Feature**: auth-v1 (`.specs/features/auth-v1/`)
- **Phase / Task**: Specify + Design + Tasks done; Execute not started
- **Completed**: Spec confirmed; design.md + tasks.md drafted (T1–T35); AD-015/016/017 appended
- **In-progress**: none
- **Next step**: In a new session: load tlc-spec-driven + Handoff; Bruno approves tasks if still Draft, then say “implementar” / Execute from T1. Prefer phase-batch sub-agents (~35 tasks → ~5 batches). Last task T35 writes `docs/study/auth-v1-backend.md`. Do not code until explicit Execute.
- **Blockers**: none (tasks Status still Draft until Bruno approves in Execute session)
- **Uncommitted files**: none after this commit (auth-v1 planning + local .env/run helpers)
- **Branch**: main

