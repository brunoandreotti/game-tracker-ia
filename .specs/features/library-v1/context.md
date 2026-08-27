# Library v1 Context

**Gathered:** 2026-08-27
**Spec:** `.specs/features/library-v1/spec.md`
**Status:** Ready for design

---

## Feature Boundary

API HTTP do diário de jogos (v1): busca RAWG sem persistir, jogos acompanhados (um `rawgId` = um registro, snapshot, status + nota), sessões com `totalMinutes` = soma. Sem UI, sem auth. Demo: Zelda → PLAYING → 90+60 minutos → `totalMinutes` 150 → nota 9 + COMPLETED.

Rotas de acompanhamento: `/tracked-games`. Busca: `/games/search`.

---

## Implementation Decisions

### Transições de status (discutido, Guided)

- Qualquer um dos quatro status (`WANT_TO_PLAY`, `PLAYING`, `COMPLETED`, `DROPPED`) pode ser gravado a partir de qualquer status atual.
- Vale para `POST /tracked-games` (status informado) e `PATCH /tracked-games/{id}`.
- Sem tabela de transições válidas, sem 409 por “salto” de jornada.
- Gravado em [docs/product.md](../../../docs/product.md) e em LIB-20.

### Arquitetura hexagonal (discutido, Guided)

- Miolo sem Spring e sem JPA. HTTP, Postgres e RAWG ficam nas bordas.
- Sempre controller → serviço → repositório. Ninguém pula camada. A busca também tem serviço.
- Serviço e repositório: interface + classe. Controller e serviço só veem a interface. O Spring injeta a classe.
- Duas portas de banco: `TrackedGameRepository` e `PlaySessionRepository`.
- Pacotes por assunto: `catalog` (busca/RAWG) e `tracking` (jogos acompanhados e sessões), cada um com `domain` / `application` / `adapter`.
- Sem uma interface por endpoint. Três serviços: busca, jogos acompanhados, sessões.

### Nomes

- Modelo do miolo: `TrackedGame`, `PlaySession`, `PlayStatus`.
- Tabela JPA: `TrackedGameEntity`, `PlaySessionEntity`.
- Contrato do serviço: `TrackedGameService`, classe `TrackedGameServiceImpl` (o mesmo padrão em `GameSearchService` e `SessionService`).
- Contrato do repositório: `TrackedGameRepository`, classe `JpaTrackedGameRepository` (o mesmo padrão em `PlaySessionRepository`).
- Catálogo: `GameCatalog` + `RawgGameCatalog` (AD-001).
- Sem prefixo `I`. Sem `Port` / `Adapter` no nome da classe.
- Rotas HTTP: `/tracked-games` e `/tracked-games/{id}/sessions`. JSON igual (sem campo `library`).

### Execute cadence (decidido)

- Uma fase completa de cada vez: Infra (T1–T5) → Search (T6–T9) → Persistence (T10–T17) → HTTP tracking (T18–T21).
- Sub-agente, se usado, pega só aquela fase. Não empilhar duas fases no mesmo lote.
- Dentro da fase: tarefa → gate → commit → próxima, sem pausa para revisão.
- Na virada de fase, perguntar e esperar o sim antes de começar a seguinte.

### Testes (decidido)

- Unitário: `TrackedGameServiceImpl` / `SessionServiceImpl` / `GameSearchServiceImpl` sem Spring.
- Integração: adapter JPA no Postgres (Testcontainers). Cliente RAWG contra WireMock.
- Componente: `@SpringBootTest` com controller, serviço e repositório reais. Só o RAWG é WireMock. Sem `@WebMvcTest` para a API.

### Agent's Discretion

- Detalhe interno do adapter JPA (Spring Data escondido atrás de `JpaTrackedGameRepository`).
- Cliente RAWG: **OpenFeign** (`@FeignClient`) + Spring Cloud **2025.1.x (Oakwood)** com Boot **4.1.1** (AD-006). Sem `@HttpExchange`.
- Texto de `message` nos erros 400/404/409/502, desde que o JSON tenha `status`, `error`, `message`.
- Pasta `.specs/features/library-v1` permanece; é só o nome do planejamento.

### Declined / Undiscussed Gray Areas → Assumptions

Estas áreas não foram escolhidas para discuss. Cada uma está na tabela Assumptions do spec, com default + razão:

- Sessão aceita em qualquer status do jogo acompanhado.
- `q` só com espaços = HTTP 400.
- `durationMinutes` só `> 0`, sem teto.
- `playedAt` futuro aceito se `YYYY-MM-DD`.
- `GET /tracked-games` por `id` crescente.
- Sessões por `playedAt` desc, depois `id` desc.
- RAWG down = HTTP 502 (v1 não usa 503).
- `year` / `coverUrl` nulos no snapshot.
- DELETE de sessão com id de jogo acompanhado errado = 404.

---

## Specific References

Contrato JSON e tabela de erros: [docs/product.md](../../../docs/product.md). Stack: [docs/stack.md](../../../docs/stack.md). Demo: buscar Zelda, duas sessões 90+60, `totalMinutes` 150.

---

## Deferred Ideas

None - discussion stayed within feature scope.
