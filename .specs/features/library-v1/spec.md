# Library v1 Specification

## Problem Statement

Bruno precisa de um diário pessoal de jogos via API HTTP, sem UI e sem login. O v1 cobre buscar no RAWG, acompanhar um jogo (status + nota) e somar minutos jogados a partir de sessões. Sem isso, o recorte “pronto quando” (Zelda → PLAYING → 150 minutos → nota 9 + COMPLETED) não tem contrato testável.

## Goals

- [ ] Completar o demo do v1 só com a API: buscar, acompanhar como PLAYING, duas sessões (90 + 60), listar com `totalMinutes` 150, PATCH nota 9 e status COMPLETED
- [ ] Expor os nove endpoints de [docs/product.md](../../../docs/product.md) com os status HTTP e JSON definidos neste spec

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
| --- | --- |
| UI / frontend | v1 é só HTTP (`curl` / cliente HTTP) |
| Cadastro, login, vários usuários | App single-user, uso local |
| Import Steam, achievements, ranks | Depois do v1 |
| Review longa, plataforma, tags | Depois do v1 |
| Estatísticas (horas no mês, média, streaks) | Depois do v1 |
| Filtro de `GET /tracked-games` por status; paginação | Fora do recorte |
| RFC 7807 (Problem Details) | Corpo de erro simples no v1 |
| Limpar nota (`rating: null`) | Sem “desavaliar” no v1 |
| Redis, Security, OpenAPI | Fora até alguém pedir |

---

## Assumptions & Open Questions

Every ambiguity is resolved or recorded here - nothing is left silently unclear.

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Transições de status | Qualquer um dos quatro status pode ser gravado a partir de qualquer status atual (PATCH e POST). Sem máquina de estados. | Discussão Guided desta área. PATCH já é um campo livre no contrato. Diário single-user não ganha com workflow. | y |
| Sessão vs status | POST de sessão é aceito em qualquer status do jogo acompanhado (WANT_TO_PLAY, PLAYING, COMPLETED, DROPPED). | Área não discutida. Horas jogadas não dependem do rótulo da jornada. | n |
| `q` só com espaços | Tratado como vazio: HTTP 400. | Área não discutida. Mesma regra de `q` vazio em product.md. | n |
| Teto de `durationMinutes` | Só `> 0` (inteiro). Sem máximo no v1. | Área não discutida. Product.md só invalida `≤ 0`. | n |
| `playedAt` no futuro | Aceito se o formato for `YYYY-MM-DD`. | Área não discutida. Sem regra de calendário no contrato. | n |
| Ordem de `GET /tracked-games` | Por `id` crescente. | Área não discutida. Lista pequena, sem paginação. | n |
| Ordem de `GET .../sessions` | Por `playedAt` descendente, depois `id` descendente. | Área não discutida. Histórico recente primeiro. | n |
| RAWG indisponível | HTTP 502 na busca e no add (timeout, conexão, 5xx do RAWG). v1 não usa 503. | Área não discutida. Um código só deixa o teste inequívoco. Product.md listava 502/503. | n |
| `year` / `coverUrl` nulos no RAWG | Snapshot grava `null` no JSON. Add não falha. | Área não discutida. Capa e ano são opcionais no catálogo. | n |
| DELETE de sessão com id de jogo acompanhado errado | HTTP 404 (sessão inexistente naquele jogo). | Área não discutida. Não vaza que a sessão existe em outro id. | n |
| Idempotência de sessão | Cada POST cria uma sessão nova. Sem chave de dedup. | N/A because o v1 trata cada log como um fato distinto. | n |
| Auth e rate limit | Ausentes. Qualquer cliente local chama qualquer endpoint. | N/A because o v1 é single-user sem autenticação. | n |
| Concorrência | Last-write-wins. Sem lock pessimista no v1. | N/A because o uso previsto é um processo local. | n |
| Expiração / soft delete | DELETE físico. Apagar o jogo acompanhado remove as sessões em cascade. | N/A because product.md já define DELETE como correção de erro. | n |
| Observabilidade | Só o log padrão do Spring Boot. Sem métricas nem tracing no v1. | N/A because o recorte não pede observabilidade. | n |
| Busca RAWG precise/exact | Sempre `search_precise=true`. `exact=true` no GET liga `search_exact`; omitido = `false`. | Fuzzy demais (Lies Of P puxava jogos só com a letra P). Exact opcional para título colado. | y |
| Testes de persistência e HTTP externo | Testcontainers PostgreSQL + WireMock no cliente RAWG. Sem H2. Domínio sem Spring. | Confirmado 2026-08-27 (AD-003). Docker é requisito de `mvn test`. | y |
| Teste de componente da API | `@SpringBootTest` com serviço e repositório reais. Só o RAWG é WireMock. Sem `@WebMvcTest`. | Confirmado 2026-08-27 (AD-004). | y |
| Rotas e nomes | HTTP `/tracked-games`. Modelo `TrackedGame`. Pacote `tracking`. | Discussão Guided. `library` não descreve o rastreio. | y |
| Pagamentos | Não se aplica. | N/A because não há cobrança. | n |
| Falha ao persistir após lookup RAWG ok | Nenhum jogo acompanhado fica gravado. HTTP 500 padrão do Boot. | Dimensão de falha parcial. Product.md não define 500; não inventar contrato além do default. | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Buscar jogos no RAWG ⭐ MVP

**User Story**: As a player, I want to search games by name on RAWG so that I can pick a `rawgId` without maintaining a local catalog.

**Why P1**: The demo starts with search. Nothing else in v1 has a catalog source.

**Acceptance Criteria** (each line is one EARS pattern):

1. WHEN the client sends `GET /games/search` with a non-empty `q` THEN the system SHALL return HTTP 200 and a JSON array whose items contain `rawgId`, `name`, `year`, and `coverUrl`.
2. WHEN the client sends `GET /games/search` THEN the system SHALL NOT persist search results as tracked games.
3. IF `q` is missing, empty, or only whitespace THEN the system SHALL return HTTP 400 and a JSON body with `status`, `error`, and `message`.
4. IF RAWG is unavailable during search THEN the system SHALL return HTTP 502 and a JSON body with `status`, `error`, and `message`.
5. WHEN RAWG returns no matches THEN the system SHALL return HTTP 200 and an empty JSON array.
6. WHEN the client sends `GET /games/search` without `exact` THEN the system SHALL call RAWG with `search_precise=true` and `search_exact=false`.
7. WHEN the client sends `GET /games/search` with `exact=true` THEN the system SHALL call RAWG with `search_precise=true` and `search_exact=true`.

**Independent Test**: Call `GET /games/search?q=zelda`, receive a non-empty array with the four fields, then `GET /tracked-games` and see that those games were not added.

---

### P1: Acompanhar jogos ⭐ MVP

**User Story**: As a player, I want to add, read, update, and delete one tracked game per RAWG id so that I can track status and rating without duplicates.

**Why P1**: The diary is the tracked game. Search alone does not record play.

**Acceptance Criteria**:

1. WHEN the client sends `POST /tracked-games` with a `rawgId` that exists on RAWG THEN the system SHALL return HTTP 201 and a JSON object with `id`, `rawgId`, `name`, `year`, `coverUrl`, `status`, `rating`, and `totalMinutes`.
2. WHEN `POST /tracked-games` omits `status` THEN the system SHALL store `status` as `PLAYING`.
3. WHEN `POST /tracked-games` includes a valid `status` THEN the system SHALL store that status.
4. WHEN `POST /tracked-games` succeeds THEN the system SHALL snapshot `name`, `year`, and `coverUrl` from RAWG at that moment and SHALL set `rating` to `null` and `totalMinutes` to `0`.
5. WHILE a tracked game exists, WHEN RAWG catalog data for that `rawgId` changes THEN the system SHALL keep the stored snapshot unchanged.
6. IF `rawgId` is already being tracked THEN the system SHALL return HTTP 409 and a JSON body with `status`, `error`, and `message`.
7. IF `rawgId` does not exist on RAWG THEN the system SHALL return HTTP 404 and a JSON body with `status`, `error`, and `message`.
8. IF RAWG is unavailable during `POST /tracked-games` THEN the system SHALL return HTTP 502 and a JSON body with `status`, `error`, and `message`.
9. WHEN the client sends `GET /tracked-games` THEN the system SHALL return HTTP 200 and all entries ordered by `id` ascending, each in the same JSON shape as the create response.
10. WHEN the client sends `GET /tracked-games/{id}` for an existing id THEN the system SHALL return HTTP 200 and the same JSON shape as a list item.
11. IF `GET /tracked-games/{id}` targets an unknown id THEN the system SHALL return HTTP 404 and a JSON body with `status`, `error`, and `message`.
12. WHEN the client sends `PATCH /tracked-games/{id}` with `status` and/or `rating` THEN the system SHALL return HTTP 200 and the updated entry.
13. WHEN `PATCH /tracked-games/{id}` includes only `status` THEN the system SHALL leave `rating` unchanged.
14. WHEN `PATCH /tracked-games/{id}` includes only `rating` THEN the system SHALL leave `status` unchanged.
15. WHEN `PATCH /tracked-games/{id}` sets `status` to any of `WANT_TO_PLAY`, `PLAYING`, `COMPLETED`, or `DROPPED` regardless of the current status THEN the system SHALL persist the new status.
16. IF the PATCH body has neither `status` nor `rating` THEN the system SHALL return HTTP 400 and a JSON body with `status`, `error`, and `message`.
17. IF `rating` is present and is not an integer in 1–10 THEN the system SHALL return HTTP 400 and a JSON body with `status`, `error`, and `message`.
18. IF `status` is present and is not one of `WANT_TO_PLAY`, `PLAYING`, `COMPLETED`, `DROPPED` THEN the system SHALL return HTTP 400 and a JSON body with `status`, `error`, and `message`.
19. IF PATCH sends `rating` as `null` THEN the system SHALL leave the stored rating unchanged.
20. WHEN the client sends `DELETE /tracked-games/{id}` for an existing id THEN the system SHALL return HTTP 204 with an empty body, remove that entry, and remove all of its sessions.
21. IF `DELETE /tracked-games/{id}` targets an unknown id THEN the system SHALL return HTTP 404 and a JSON body with `status`, `error`, and `message`.

**Independent Test**: Add a known `rawgId` without status (expect PLAYING, rating null, totalMinutes 0), GET list and by id, PATCH rating then status through all four values, POST the same `rawgId` again (409), DELETE (204), GET by id (404).

---

### P1: Registrar sessões e totalMinutes ⭐ MVP

**User Story**: As a player, I want to log play sessions in minutes on a calendar date so that tracked-game totals reflect time actually played.

**Why P1**: The demo requires two sessions summing to 150. Hours are the sum of sessions, not a stored override.

**Acceptance Criteria**:

1. WHEN the client sends `POST /tracked-games/{id}/sessions` with `durationMinutes` greater than 0 THEN the system SHALL return HTTP 201 and a JSON object with `id`, `durationMinutes`, and `playedAt`.
2. WHEN `POST /tracked-games/{id}/sessions` omits `playedAt` THEN the system SHALL set `playedAt` to the server’s current date in `YYYY-MM-DD`.
3. WHEN `POST /tracked-games/{id}/sessions` includes a valid `playedAt` THEN the system SHALL store that date.
4. WHEN a session is created THEN the system SHALL increase the tracked game’s `totalMinutes` by that session’s `durationMinutes`.
5. WHEN the client sends `GET /tracked-games/{id}/sessions` THEN the system SHALL return HTTP 200 and that game’s sessions ordered by `playedAt` descending then `id` descending.
6. WHEN the client sends `DELETE /tracked-games/{id}/sessions/{sessionId}` for a session that belongs to that tracked game THEN the system SHALL return HTTP 204 with an empty body and decrease `totalMinutes` by that session’s `durationMinutes`.
7. WHEN `POST /tracked-games/{id}/sessions` targets a tracked game in any status THEN the system SHALL create the session.
8. IF `durationMinutes` is missing or is less than or equal to 0 THEN the system SHALL return HTTP 400 and a JSON body with `status`, `error`, and `message`.
9. IF `playedAt` is present and is not a calendar date `YYYY-MM-DD` THEN the system SHALL return HTTP 400 and a JSON body with `status`, `error`, and `message`.
10. IF a session endpoint targets an unknown tracked-game id THEN the system SHALL return HTTP 404 and a JSON body with `status`, `error`, and `message`.
11. IF `DELETE /tracked-games/{id}/sessions/{sessionId}` targets a session id that does not belong to that tracked-game id THEN the system SHALL return HTTP 404 and a JSON body with `status`, `error`, and `message`.

**Independent Test**: Add a game, POST sessions 90 and 60, GET the entry with `totalMinutes` 150, GET sessions, DELETE one session, GET the entry with `totalMinutes` equal to the remaining session.

---

## Edge Cases

- IF RAWG returns a game with `year` or `coverUrl` null THEN the system SHALL still create the tracked game and SHALL serialize those fields as JSON `null`.
- IF `GET /tracked-games` runs against an empty list THEN the system SHALL return HTTP 200 and an empty JSON array.
- IF `playedAt` is a future `YYYY-MM-DD` THEN the system SHALL accept the session.
- IF two `POST /tracked-games/{id}/sessions` use the same `playedAt` THEN the system SHALL create two sessions.
- IF persistence fails after a successful RAWG lookup on `POST /tracked-games` THEN the system SHALL NOT persist a tracked game.

---

## Requirement Traceability

Each requirement gets a unique ID for tracking across design, tasks, and validation.

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| LIB-01 | P1: Buscar jogos no RAWG | Tasks | In Tasks |
| LIB-02 | P1: Buscar jogos no RAWG | Tasks | In Tasks |
| LIB-03 | P1: Buscar jogos no RAWG | Tasks | In Tasks |
| LIB-04 | P1: Buscar jogos no RAWG | Tasks | In Tasks |
| LIB-05 | P1: Buscar jogos no RAWG | Tasks | In Tasks |
| LIB-06 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-07 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-08 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-09 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-10 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-11 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-12 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-13 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-14 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-15 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-16 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-17 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-18 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-19 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-20 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-21 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-22 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-23 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-24 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-25 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-26 | P1: Acompanhar jogos | Tasks | In Tasks |
| LIB-27 | P1: Registrar sessões e totalMinutes | Tasks | In Tasks |
| LIB-28 | P1: Registrar sessões e totalMinutes | Tasks | In Tasks |
| LIB-29 | P1: Registrar sessões e totalMinutes | Tasks | In Tasks |
| LIB-30 | P1: Registrar sessões e totalMinutes | Tasks | In Tasks |
| LIB-31 | P1: Registrar sessões e totalMinutes | Tasks | In Tasks |
| LIB-32 | P1: Registrar sessões e totalMinutes | Tasks | In Tasks |
| LIB-33 | P1: Registrar sessões e totalMinutes | Tasks | In Tasks |
| LIB-34 | P1: Registrar sessões e totalMinutes | Tasks | In Tasks |
| LIB-35 | P1: Registrar sessões e totalMinutes | Tasks | In Tasks |
| LIB-36 | P1: Registrar sessões e totalMinutes | Tasks | In Tasks |
| LIB-37 | P1: Registrar sessões e totalMinutes | Tasks | In Tasks |

**ID format:** `LIB-NN`

**Status values:** Pending → In Design → In Tasks → Implementing → Verified

**Coverage:** 37 total, 37 mapped to tasks, 0 unmapped

---

## Success Criteria

How we know the feature is successful:

- [ ] Demo path: search Zelda, track as PLAYING, two sessions (90 + 60), list shows `totalMinutes` 150, PATCH rating 9 and status COMPLETED
- [ ] Duplicate `rawgId` returns 409; unknown ids return 404; invalid input returns 400; RAWG down returns 502
- [ ] Status may change to any of the four values from any current value
