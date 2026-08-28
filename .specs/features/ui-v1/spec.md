# UI v1 Specification

## Problem Statement

A API do diário de jogos já cobre busca, acompanhamento e sessões, mas o uso ainda depende de `curl`/Bruno. Bruno quer uma UI local simples (e aprender React no processo) para o happy path: buscar → acompanhar → ver lista → detalhe (status/nota) → logar/apagar sessão.

## Goals

- [ ] Completar o demo do diário só pela UI: buscar um jogo, acompanhar, registrar duas sessões, ver total de minutos, alterar nota e status
- [ ] Entregar SPA em `frontend/` (React + Vite + TypeScript) contra a API existente, com rotas lista / busca / detalhe e visual de diário minimal

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
| --- | --- |
| Mover backend para `api/` | Decidido adiar; só `frontend/` na raiz por enquanto |
| Next.js, SSR, SSG | SPA local basta; menos conceitos para aprender |
| Tailwind / design system | CSS enxuto no v1 para legibilidade |
| Login, multi-user, auth | App single-user local |
| Stats, filtro por status, paginação | Fora do contrato da API e do recorte UI |
| PWA, app mobile nativo | Desktop-first local |
| Import Steam, review longa, plataforma, tags | Depois |
| Alterar contrato JSON da API | UI consome o contrato de library-v1 / product.md |

---

## Assumptions & Open Questions

Every ambiguity is resolved or recorded here - nothing is left silently unclear.

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Pasta do front | `frontend/` na raiz do repo; backend permanece na raiz | Discussão Guided; menos churn que `api/` + `frontend/` agora | y |
| Escopo da 1ª UI | Happy path completo: busca, acompanhar, lista, detalhe (status/nota), sessões (criar/apagar), apagar jogo acompanhado | Sem busca/add a lista fica vazia sem curl | y |
| Stack | React + Vite + TypeScript | Bruno conhece TS; agente guia o framework; ecossistema amplo | y |
| Navegação | `/` lista; `/search` busca; `/games/:id` detalhe (`id` = id do jogo acompanhado) | Lista→detalhe + busca separada, mais fácil de aprender | y |
| Tom visual | Diário pessoal minimal; capas modestas; pouco chrome | Não é clone Steam/loja | y |
| Idioma da UI | Português | Docs e produto em PT; usuário único é Bruno | y |
| Estilo CSS | CSS Modules e/ou CSS global com variáveis; sem Tailwind no v1 | Código mais explícito enquanto aprende React | y |
| Desktop-first | Layout pensado para desktop; sem app mobile | Uso local previsto | y |
| Base URL da API | Variável `VITE_API_URL` (ex.: `http://localhost:8080`) | Separar porta do Vite da do Spring | y |
| CORS | Backend passa a permitir a origem do Vite quando a UI for implementada | Necessário para SPA em outra porta; detalhe na Design/Execute | y |
| Toggle `exact` na busca | Checkbox opcional “busca exata” mapeando `exact=true` | Espelha a API; default desligado | n |
| Exibir `totalMinutes` | Formato legível (ex. `2h 30min`) além ou no lugar do número cru | Demo humano; API continua em minutos | n |
| Labels de status | Português na UI (`Quero jogar`, `Jogando`, `Zerei`, `Dropado`); valores enviados à API permanecem os enums | Diário em PT; contrato HTTP intacto | n |
| Confirmação antes de DELETE | Confirm dialog nativo do browser antes de apagar jogo ou sessão | Evita clique acidental; single-user | n |
| Empty state da lista | Mensagem + link/CTA para `/search` | Happy path começa na busca | n |
| Erros da API | Mostrar `message` do JSON de erro quando existir; senão mensagem genérica em PT | Contrato `{ status, error, message }` | n |
| Loading | Indicador simples (texto ou spinner mínimo) enquanto fetch pendente | Evita UI “morta” | n |
| Auth / rate limit | Ausentes | N/A because API e app são single-user local | n |
| Pagamentos | Não se aplica | N/A because não há cobrança | n |
| Concorrência na UI | Uma aba; last-write-wins como a API | N/A because uso local de um processo | n |
| Offline / PWA | Sem suporte offline | N/A because fora do escopo | n |
| Testes do front | Definidos na Design (ex. Vitest + Testing Library para fluxos críticos) | Escopo de ferramentas de teste é decisão de Design | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Ver jogos acompanhados ⭐ MVP

**User Story**: As a player, I want to see my tracked games on the home page so that I know what I am following without using curl.

**Why P1**: Home is the diary entry point.

**Acceptance Criteria**:

1. WHEN the user opens `/` THEN the system SHALL call `GET /tracked-games` and display each entry with at least name, status label in Portuguese, rating (or indication that it is unset), and total play time derived from `totalMinutes`.
2. WHEN `GET /tracked-games` returns an empty array THEN the system SHALL show an empty-state message in Portuguese and a way to navigate to `/search`.
3. WHEN the user activates a tracked game in the list THEN the system SHALL navigate to `/games/{id}` for that entry's `id`.
4. IF `GET /tracked-games` fails THEN the system SHALL show an error message in Portuguese and SHALL NOT show a fabricated list.
5. WHILE `GET /tracked-games` is in progress THEN the system SHALL show a loading indicator.

**Independent Test**: With two tracked games in the API, open `/` and see both; with none, see empty state and reach `/search`.

---

### P1: Buscar e acompanhar ⭐ MVP

**User Story**: As a player, I want to search RAWG from the UI and start tracking a game so that the diary can be filled without a HTTP client.

**Why P1**: Without add, the list stays empty unless curl is used.

**Acceptance Criteria**:

1. WHEN the user opens `/search` THEN the system SHALL show a search field and a submit control labeled in Portuguese.
2. WHEN the user submits a non-empty query THEN the system SHALL call `GET /games/search?q={query}` and display results with name, year, and cover when `coverUrl` is present.
3. WHEN the user enables exact search and submits THEN the system SHALL call `GET /games/search` with `exact=true`.
4. WHEN the user chooses to track a result THEN the system SHALL call `POST /tracked-games` with that `rawgId` (status omitted so API defaults to `PLAYING`) and THEN navigate to `/games/{id}` of the created entry.
5. WHEN search returns an empty array THEN the system SHALL show a no-results message in Portuguese.
6. IF the user submits an empty or whitespace-only query THEN the system SHALL NOT call the API and SHALL show a validation message in Portuguese.
7. IF `POST /tracked-games` returns 409 THEN the system SHALL show a message that the game is already tracked and SHALL offer navigation to the existing entry when the UI can resolve it, or to `/` otherwise.
8. IF search or add fails with another API error THEN the system SHALL show the API `message` when present, otherwise a generic Portuguese error.
9. WHILE a search or add request is in progress THEN the system SHALL show a loading indicator and SHALL prevent duplicate submit of the same action.

**Independent Test**: Search “zelda”, track one result, land on detail; search again for the same and see 409 handling.

---

### P1: Detalhe, status, nota e sessões ⭐ MVP

**User Story**: As a player, I want a detail page to change status and rating, log sessions, and remove mistakes so that the diary stays accurate.

**Why P1**: Completes the “pronto quando” demo through the UI.

**Acceptance Criteria**:

1. WHEN the user opens `/games/{id}` for an existing tracked game THEN the system SHALL load `GET /tracked-games/{id}` and `GET /tracked-games/{id}/sessions` and show name, year, cover when present, status, rating, total play time, and the session list.
2. WHEN the user changes status to any of the four values THEN the system SHALL call `PATCH /tracked-games/{id}` with that `status` and refresh the displayed status.
3. WHEN the user sets a rating from 1 to 10 THEN the system SHALL call `PATCH /tracked-games/{id}` with that `rating` and refresh the displayed rating.
4. WHEN the user submits a new session with `durationMinutes` > 0 THEN the system SHALL call `POST /tracked-games/{id}/sessions` (optional `playedAt` as `YYYY-MM-DD`; omit to let the API default to today) and refresh sessions and total play time.
5. WHEN the user confirms deletion of a session THEN the system SHALL call `DELETE /tracked-games/{id}/sessions/{sessionId}` and refresh sessions and total play time.
6. WHEN the user confirms deletion of the tracked game THEN the system SHALL call `DELETE /tracked-games/{id}` and THEN navigate to `/`.
7. IF `GET /tracked-games/{id}` returns 404 THEN the system SHALL show a not-found message in Portuguese and a way to return to `/`.
8. IF session create is submitted with `durationMinutes` ≤ 0 or non-numeric THEN the system SHALL NOT call the API and SHALL show a validation message in Portuguese.
9. IF any mutating request fails THEN the system SHALL show an error in Portuguese and SHALL leave the previous successful data on screen until a successful refresh.
10. The system SHALL require an explicit confirm step before calling DELETE for a session or for the tracked game.

**Independent Test**: Open a tracked game, add 90 and 60 minute sessions, see total reflecting 150 minutes, set rating 9 and status COMPLETED, delete one session and see total update, delete the game and land on `/`.

---

### P2: Navegação e chrome mínimo

**User Story**: As a player, I want simple navigation between list, search, and detail so that I can move through the diary without clutter.

**Why P2**: Supports P1 flows; can ship with links already required by P1.

**Acceptance Criteria**:

1. The system SHALL provide navigation to `/` and `/search` from the main chrome in Portuguese.
2. WHEN the user is on a detail page THEN the system SHALL provide a way to return to `/` without using the browser back button alone.
3. The system SHALL use a minimal diary visual style: restrained chrome, modest covers, no store/Steam-like dense catalog chrome on the home list.

**Independent Test**: From list go to search and back; from detail return to list; visual review matches “diário minimal”.

---

## Edge Cases

- IF the API is unreachable (network error) THEN the system SHALL show a Portuguese error indicating the API could not be reached.
- IF `coverUrl` is null THEN the system SHALL render the entry without a broken image (placeholder or omit image).
- IF `rating` is null THEN the system SHALL show that no rating is set (not `0`).
- WHEN `totalMinutes` is `0` THEN the system SHALL show a zero / no-time indication consistent with the chosen display format.
- IF PATCH or POST validation fails on the server (400) THEN the system SHALL show the API `message` when present.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| UI-01 | P1: Ver jogos acompanhados | Execute | ✅ Verified |
| UI-02 | P1: Ver jogos acompanhados | Execute | ✅ Verified |
| UI-03 | P1: Ver jogos acompanhados | Execute | ✅ Verified |
| UI-04 | P1: Ver jogos acompanhados | Execute | ✅ Verified |
| UI-05 | P1: Ver jogos acompanhados | Execute | ✅ Verified |
| UI-06 | P1: Buscar e acompanhar | Execute | ✅ Verified |
| UI-07 | P1: Buscar e acompanhar | Execute | ✅ Verified |
| UI-08 | P1: Buscar e acompanhar | Execute | ✅ Verified |
| UI-09 | P1: Buscar e acompanhar | Execute | ✅ Verified |
| UI-10 | P1: Buscar e acompanhar | Execute | ✅ Verified |
| UI-11 | P1: Buscar e acompanhar | Execute | ✅ Verified |
| UI-12 | P1: Buscar e acompanhar | Execute | ✅ Verified |
| UI-13 | P1: Buscar e acompanhar | Execute | ✅ Verified |
| UI-14 | P1: Buscar e acompanhar | Execute | ✅ Verified |
| UI-15 | P1: Detalhe, status, nota e sessões | Execute | ✅ Verified |
| UI-16 | P1: Detalhe, status, nota e sessões | Execute | ✅ Verified |
| UI-17 | P1: Detalhe, status, nota e sessões | Execute | ✅ Verified |
| UI-18 | P1: Detalhe, status, nota e sessões | Execute | ✅ Verified |
| UI-19 | P1: Detalhe, status, nota e sessões | Execute | ✅ Verified |
| UI-20 | P1: Detalhe, status, nota e sessões | Execute | ✅ Verified |
| UI-21 | P1: Detalhe, status, nota e sessões | Execute | ✅ Verified |
| UI-22 | P1: Detalhe, status, nota e sessões | Execute | ✅ Verified |
| UI-23 | P1: Detalhe, status, nota e sessões | Execute | ✅ Verified |
| UI-24 | P1: Detalhe, status, nota e sessões | Execute | ✅ Verified |
| UI-25 | P2: Navegação e chrome mínimo | Execute | ✅ Verified |
| UI-26 | P2: Navegação e chrome mínimo | Execute | ✅ Verified |
| UI-27 | P2: Navegação e chrome mínimo | Execute | ⚠️ UAT (visual diary) |

**Coverage:** 27 total, 26 verified by automated evidence, 1 pending interactive UAT (visual).

---

## Success Criteria

- [ ] Demo completo só pela UI: buscar → acompanhar → duas sessões → total coerente → nota 9 + COMPLETED
- [ ] Código em `frontend/` com React + Vite + TypeScript; backend não movido para `api/`
- [ ] Rotas `/`, `/search`, `/games/:id` funcionando contra a API local
- [ ] Visual reconhecível como diário minimal (não loja)
- [ ] Bruno consegue seguir o fluxo com explicações curtas do agente quando necessário
