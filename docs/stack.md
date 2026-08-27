# Stack (planejada)

Produção: **Java 21**, Spring Boot **4.1.1** (parent atual do `pom.xml`). Scaffold hoje: Web MVC, driver PostgreSQL (sem JPA), Lombok.

Nada disto está no código ainda. Versões finas se cravam na implementação, com Context7.

## Testes: Spock + Groovy

- Specs em `src/test/groovy` (mesmo pacote da feature). Produção continua Java.
- Spock 2.4 no JUnit Platform; `spock-spring` (`@SpringBean` / `@SpringSpy`) só quando o spec subir Spring.
- Context7 (`/spockframework/spock`): 2.4 e módulo Spring ok. **Não** veio Maven + Groovy 4 + Java 21 — gate na implementação (`spock-core` / `spock-spring`, classifier Groovy 4).
- Preferência: regras de domínio **sem** contexto Spring; API/JPA só no slice que precisar.

## Docker Compose

- Compose com **Postgres + aplicação**.
- No dia a dia: `mvnw spring-boot:run` contra o Postgres do Compose (ciclo mais curto que rebuild de imagem).
- RAWG **não** entra no Compose (API externa). Chave só no env (`RAWG_API_KEY` ou equivalente).
- Testcontainers para `mvn test`: evolução, não obrigação do v1.

## Cliente HTTP: Feign, com gate

- Preferência: OpenFeign (`@EnableFeignClients`, `@FeignClient`) para search + getById do RAWG.
- Isolar atrás de uma porta nossa (`GameCatalog`); o resto da app não conhece Feign/RAWG.
- Context7 (`/spring-cloud/spring-cloud-openfeign`, `main`): BOM `spring-cloud-openfeign-dependencies` **5.0.3-SNAPSHOT**. **Não** há matriz clara Boot **4.1.1** ↔ release train.
- **Gate:** BOM Cloud estável alinhado ao 4.1.1. Se não houver, fallback nativo do Boot **4.0.0+**: `@HttpExchange` / `@GetExchange`, `@ImportHttpServices`, `spring.http.serviceclient.*.base-url` (docs `/spring-projects/spring-boot/v4.1.0`). Sem Spring Cloud.

## Persistência

- **Spring Data JPA** (`spring-boot-starter-data-jpa`) — confirmado no Boot 4.x via Context7. Driver Postgres já está no `pom`.
- Schema: **Flyway** sugerido (Compose como jeito oficial de subir o banco); Hibernate `ddl-auto` só como atalho local, se escolhido depois.
- Validação: `spring-boot-starter-validation` nos DTOs (nota 1–10, `durationMinutes` > 0) — sugerido, ainda não decidido.

## Fora até alguém pedir

Security, OpenAPI, Redis.

## Context7

Usar o MCP Context7 (`resolve-library-id` → `query-docs`) para docs de lib na implementação. Não commitar chave/token do MCP no git do projeto.
