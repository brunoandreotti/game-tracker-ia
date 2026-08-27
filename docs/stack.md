# Stack (planejada)

Produção: **Java 21**, Spring Boot **4.1.1** (parent atual do `pom.xml`), Spring Cloud **2025.1.x (Oakwood)** para OpenFeign.

Nada disto está no código ainda. Versões finas se cravam na implementação, com Context7.

## Testes: JUnit 5 + Mockito

- Testes em `src/test/java` (mesmo pacote da feature).
- Unitários: JUnit 5 + Mockito, sem Spring.
- Integração/componente: `@SpringBootTest`, Testcontainers, WireMock — bases em `src/test/java/.../config/`.

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
- Validação: **`spring-boot-starter-validation`** nos DTOs (`q` não vazio, nota 1–10, `durationMinutes` > 0).

## Fora até alguém pedir

Security, OpenAPI, Redis.

## Context7

Usar o MCP Context7 (`resolve-library-id` → `query-docs`) para docs de lib na implementação. Não commitar chave/token do MCP no git do projeto.
