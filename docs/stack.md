# Stack (planejada)

Produção: **Java 21**, Spring Boot **4.1.1** (parent atual do `pom.xml`), Spring Cloud **2025.1.x (Oakwood)** para OpenFeign.

Nada disto está no código ainda. Versões finas se cravam na implementação, com Context7.

## Testes: Spock 2.4

- Feature tests in `src/test/groovy` (`*Spec.groovy`, same package as production). `contextLoads` stays JUnit in `src/test/java`.
- Feature method names: English `Given ..., When ..., Then ...`. Blocks `given:` / `when:` / `then:` each with an English description. Prefer `when`/`then` over `expect`.
- Unitários: Spock, sem Spring.
- Integração/componente: Spock + `@SpringBootTest`, Testcontainers, WireMock — bases em `src/test/java/.../config/` e `src/test/groovy/.../config/`.

## Testes: três níveis

- **Unitário:** `GameSearchServiceImpl` / `TrackedGameServiceImpl` / `SessionServiceImpl` sem Spring. Sem banco e sem HTTP.
- **Integração:** adapter JPA no Postgres (Testcontainers). Cliente do RAWG contra WireMock. Sem H2.
- **Componente:** `@SpringBootTest`. Controller, serviço e repositório reais. Postgres no Testcontainers. Só o RAWG vira WireMock. Não fakeia serviço nem repositório. Sem `@WebMvcTest` para o contrato da API.
- `mvn test` precisa de Docker. Compose continua só para `spring-boot:run`.
- Versões finas na implementação, com Context7.

## Docker Compose

- Um `compose.yaml` **na raiz** com **Postgres + aplicação**.
- No dia a dia: `mvn spring-boot:run` contra o Postgres do Compose (ciclo mais curto que rebuild de imagem).
- RAWG **não** entra no Compose (API externa). Chave só no env (`RAWG_API_KEY` ou equivalente).

## Cliente HTTP: Feign (OpenFeign)

- **OpenFeign** (`@EnableFeignClients`, `@FeignClient`) para search + getById do RAWG.
- Isolar atrás da porta `GameCatalogPort`; só o adapter do catálogo conhece Feign/RAWG. O controller de busca chama `GameSearchService`, não a porta.
- **Spring Boot 4.1.1** + **Spring Cloud 2025.1.x (Oakwood)** BOM `spring-cloud-dependencies` — matriz oficial suporta Boot 4.1.x (desde 2025.1.2). Sem downgrade.
- Config: `rawg.base-url`, `rawg.api-key`; timeouts em `spring.cloud.openfeign.client.config.rawg`.

## Persistência

- **Spring Data JPA** (`spring-boot-starter-data-jpa`) — confirmado no Boot 4.x via Context7. Driver Postgres já está no `pom`.
- Schema: **Flyway** no v1. Sem Hibernate `ddl-auto` no jeito oficial (Compose + migrações).
- Validação: **`spring-boot-starter-validation`** nos DTOs (`q` não vazio, nota **0–5**, `durationMinutes` > 0).

## Frontend (UI v1)

- Pasta: **`frontend/`** na raiz do repo (backend Spring permanece na raiz; sem `api/` por agora).
- **React + Vite + TypeScript** (SPA). Sem Next.js no v1.
- Estilo (estudo, AD-013): **Tailwind CSS + shadcn/ui** (componentes copiados para `frontend/src/components/ui/`). Tema dark Letterboxd (AD-012). CSS global legado pode coexistir na migração e ser reduzido depois.
- Dev: Vite em porta própria (`5173`); `VITE_API_URL` aponta para o Spring. CORS no backend.
- UI em português; desktop-first.
- Spec: [`.specs/features/ui-v1`](../.specs/features/ui-v1/spec.md).
- Código do front só quando Bruno pedir para implementar / evoluir.

## Fora até alguém pedir

Security, OpenAPI, Redis, Next.js, kits tipo antd/MUI (shadcn escolhido em vez deles).

## Context7

Usar o MCP Context7 (`resolve-library-id` → `query-docs`) para docs de lib na implementação. Não commitar chave/token do MCP no git do projeto.
