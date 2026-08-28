# UI v1 Validation

**Date**: 2026-08-28
**Spec**: `.specs/features/ui-v1/spec.md`
**Diff range**: `aadca65^..a6bc905`
**Verifier**: independent sub-agent (author ≠ verifier)

## Validation

**Result**: PASS

---

## Task Completion

| Task | Status | Notes |
| ---- | ------ | ----- |
| T1 | ✅ Done | Scaffold Vite React TS |
| T2 | ✅ Done | VITE_API_URL |
| T3 | ✅ Done | Spring CORS |
| T4 | ✅ Done | API DTO types |
| T5 | ✅ Done | apiClient + tests |
| T6 | ✅ Done | gamesApi + tests |
| T7 | ✅ Done | formatMinutes + tests |
| T8 | ✅ Done | playStatus + tests |
| T9 | ✅ Done | global CSS (build gate) |
| T10 | ✅ Done | Feedback/Cover + CoverImage tests |
| T11 | ✅ Done | AppLayout + nav tests |
| T12 | ✅ Done | TrackedGamesPage |
| T13 | ✅ Done | SearchPage |
| T14 | ✅ Done | TrackedGameDetailPage |
| T15 | ✅ Done | Routes wired |
| T16 | ✅ Done | List happy/error/loading tests |
| T17 | ✅ Done | Search results/empty/error/loading/track-guard tests |
| T18 | ✅ Done | Detail status/rating/session/delete/patch-failure tests |
| T19 | ✅ Done | AppLayout nav + CoverImage null tests |
| T20 | ✅ Done | Close Verifier AC gaps (session delete refresh, 409→`/`, add error, list GET, detail cover) |

All T1–T20 Done-when checkboxes are `[x]`.

---

## Spec-Anchored Acceptance Criteria

### P1: Ver jogos acompanhados

| Criterion (WHEN X THEN Y) | Spec-defined outcome | `file:line` + assertion | Result |
| ------------------------- | -------------------- | ----------------------- | ------ |
| WHEN user opens `/` THEN call `GET /tracked-games` and display name, PT status, rating/unset, total play time | List fetch + render name + PT status + `Sem nota`/rating + formatted minutes | `frontend/src/pages/TrackedGamesPage.test.tsx:70-80` - `getByText('Game One')`, `/Jogando/`, `/Sem nota/`, `/45 min/`, `/2h/`; `:80` - `expect(gamesApi.listTrackedGames).toHaveBeenCalled()` | ✅ PASS |
| WHEN `GET /tracked-games` returns `[]` THEN empty-state PT + navigate to `/search` | PT empty message + link `href="/search"` | `frontend/src/pages/TrackedGamesPage.test.tsx:30` - `getByText('Você ainda não acompanha nenhum jogo.')`; `:33` - `getByRole('link', { name: 'Buscar jogos' })).toHaveAttribute('href', '/search')` | ✅ PASS |
| WHEN user activates a tracked game THEN navigate to `/games/{id}` | Link to entry id | `frontend/src/pages/TrackedGamesPage.test.tsx:78-79` - `getByRole('link', { name: /Game One/ })).toHaveAttribute('href', '/games/1')` | ✅ PASS |
| IF `GET /tracked-games` fails THEN PT error and no fabricated list | Error alert; no list heading or game links | `frontend/src/pages/TrackedGamesPage.test.tsx:94-99` - `getByRole('alert')).toHaveTextContent('Servidor indisponível')`; `queryByRole('heading', { name: 'Meus jogos' })).not.toBeInTheDocument()` | ✅ PASS |
| WHILE `GET /tracked-games` in progress THEN loading indicator | Loading text visible | `frontend/src/pages/TrackedGamesPage.test.tsx:116` - `getByText('Carregando jogos...')` | ✅ PASS |

### P1: Buscar e acompanhar

| Criterion (WHEN X THEN Y) | Spec-defined outcome | `file:line` + assertion | Result |
| ------------------------- | -------------------- | ----------------------- | ------ |
| WHEN user opens `/search` THEN search field + submit labeled PT | Field + submit in Portuguese | `frontend/src/pages/SearchPage.test.tsx:42` - `getByRole('button', { name: 'Buscar' })`; `:57` - `getByLabelText('Nome do jogo')` | ✅ PASS |
| WHEN submits non-empty query THEN `GET /games/search?q=` and display name, year, cover when present | Results show name/year/cover img | `frontend/src/pages/SearchPage.test.tsx:104-110` - `getByText('Hollow Knight')`, `getByText('2017')`, `getByRole('img', { name: 'Hollow Knight' })).toHaveAttribute('src', 'https://example.com/cover.jpg')` | ✅ PASS |
| WHEN exact enabled and submits THEN `exact=true` | `searchGames(q, true)` | `frontend/src/pages/SearchPage.test.tsx:79` - `expect(gamesApi.searchGames).toHaveBeenCalledWith('Zelda', true)` | ✅ PASS |
| WHEN user tracks a result THEN `POST /tracked-games` with `rawgId` only and navigate `/games/{id}` | POST rawgId; navigate created id | `frontend/src/pages/SearchPage.test.tsx:214-215` - `createTrackedGame).toHaveBeenCalledWith(42)` + `navigateMock).toHaveBeenCalledWith('/games/7')`; `frontend/src/api/gamesApi.test.ts:54-57` - POST body `{ rawgId: 123 }` | ✅ PASS |
| WHEN search returns `[]` THEN no-results message PT | `Nenhum jogo encontrado.` | `frontend/src/pages/SearchPage.test.tsx:128` - `getByText('Nenhum jogo encontrado.')` | ✅ PASS |
| IF empty/whitespace query THEN NOT call API + PT validation | No API; validation message | `frontend/src/pages/SearchPage.test.tsx:44-45` - `searchGames).not.toHaveBeenCalled()` + alert `'Digite um termo de busca.'`; `:60-61` whitespace | ✅ PASS |
| IF `POST` returns 409 THEN already-tracked message + nav to existing or `/` | Conflict PT + link to game or home | Resolved: `frontend/src/pages/SearchPage.test.tsx:301-305` - alert `'Este jogo já está na sua lista.'` + link `href='/games/3'`; unresolved: `:340-341` - link `'Ir para meus jogos'` `href='/'` | ✅ PASS |
| IF search/add fails with other API error THEN show API `message` or generic PT | Error from API | Search: `frontend/src/pages/SearchPage.test.tsx:146` - `getByRole('alert')).toHaveTextContent('RAWG indisponível')`; add: `:373` - `'Falha no servidor'` | ✅ PASS |
| WHILE search/add in progress THEN loading + prevent duplicate submit | Loading text + disabled/guard | Search: `frontend/src/pages/SearchPage.test.tsx:168-169` - `getByText('Buscando jogos...')` + `button Buscar).toBeDisabled()`; Track: `:253-256` - `Acompanhando...` + disabled Acompanhar | ✅ PASS |

### P1: Detalhe, status, nota e sessões

| Criterion (WHEN X THEN Y) | Spec-defined outcome | `file:line` + assertion | Result |
| ------------------------- | -------------------- | ----------------------- | ------ |
| WHEN opens `/games/{id}` THEN load game+sessions and show name/year/cover/status/rating/total/sessions | Full detail after dual GET | Core: `frontend/src/pages/TrackedGameDetailPage.test.tsx:63-71` - heading, back link, meta `2017 · Jogando · Sem nota · 0 min`, sessions empty; cover: `:84-86` - img `src='https://example.com/cover.jpg'` | ✅ PASS |
| WHEN changes status THEN `PATCH` with status and refresh | PATCH + updated PT status in UI | `frontend/src/pages/TrackedGameDetailPage.test.tsx:122-123` - `patchTrackedGame).toHaveBeenCalledWith(1, { status: 'COMPLETED' })` + `/Zerei/` | ✅ PASS |
| WHEN sets rating 1–10 THEN `PATCH` with rating and refresh | PATCH + updated rating in UI | `frontend/src/pages/TrackedGameDetailPage.test.tsx:145-146` - `patchTrackedGame).toHaveBeenCalledWith(1, { rating: 9 })` + `/Nota 9/` | ✅ PASS |
| WHEN submits session `durationMinutes` > 0 THEN POST + refresh sessions/total | POST + refreshed total | `frontend/src/pages/TrackedGameDetailPage.test.tsx:174-175` - `createSession).toHaveBeenCalledWith(1, { durationMinutes: 90 })` + `/1h 30min/` | ✅ PASS |
| WHEN confirms session delete THEN DELETE + refresh sessions/total | confirm + DELETE + UI refresh | `frontend/src/pages/TrackedGameDetailPage.test.tsx:221-223` - `deleteSession).toHaveBeenCalledWith(1, 10)` + `'Nenhuma sessão registrada.'` + meta `/0 min/` | ✅ PASS |
| WHEN confirms game delete THEN DELETE + navigate `/` | confirm + DELETE + `/` | `frontend/src/pages/TrackedGameDetailPage.test.tsx:244-246` - `confirm` + `deleteTrackedGame).toHaveBeenCalledWith(1)` + `navigateMock).toHaveBeenCalledWith('/')` | ✅ PASS |
| IF GET game 404 THEN not-found PT + way to `/` | PT not-found + home link | `frontend/src/pages/TrackedGameDetailPage.test.tsx:300-303` - `'Jogo não encontrado.'` + link `href='/'` | ✅ PASS |
| IF session create invalid duration THEN NOT call API + PT validation | No `createSession`; validation alert | `frontend/src/pages/TrackedGameDetailPage.test.tsx:287-289` - `createSession).not.toHaveBeenCalled()` + duration alert | ✅ PASS |
| IF mutating request fails THEN PT error and keep previous data | Error alert + heading remains | `frontend/src/pages/TrackedGameDetailPage.test.tsx:268-269` - alert `'Falha ao atualizar status'` + heading `'The Legend of Zelda'` | ✅ PASS |
| SHALL require explicit confirm before DELETE session or game | `window.confirm` before DELETE | Session declined: `:195-196` - `confirm).toHaveBeenCalledWith('Remover esta sessão?')` + `deleteSession).not.toHaveBeenCalled()`; Game: `:244` - `confirm).toHaveBeenCalledWith('Remover este jogo da sua lista?')` | ✅ PASS |

### P2: Navegação e chrome mínimo

| Criterion (WHEN X THEN Y) | Spec-defined outcome | `file:line` + assertion | Result |
| ------------------------- | -------------------- | ----------------------- | ------ |
| SHALL provide nav to `/` and `/search` from chrome in PT | PT nav links | `frontend/src/components/AppLayout.test.tsx:20-21` - `getByRole('link', { name: 'Meus jogos' })).toHaveAttribute('href', '/')` + `Buscar → /search` | ✅ PASS |
| WHEN on detail THEN way to return to `/` | Back link on detail | `frontend/src/pages/TrackedGameDetailPage.test.tsx:66-68` - `getByRole('link', { name: '← Voltar para meus jogos' })).toHaveAttribute('href', '/')` | ✅ PASS |
| SHALL use minimal diary visual style | Restrained chrome, modest covers, not store-like | Visual judgment; CSS/build gate only | ⚠️ UAT-needed |

**Status**: ✅ All P1 ACs evidenced; P2-3 visual remains UAT-only (does not block PASS)

**Counts**: 26/27 ACs with automated test evidence ✅ · 1 ⚠️ UAT/visual (P2-3)

---

## Discrimination Sensor

Baseline porcelain captured before sensor; real tree unchanged after cleanup: **YES**.

Scratch: `git worktree add` → `%TEMP%\ui-v1-sensor-wt*` at `a6bc905`; `node_modules` junction to main `frontend/node_modules` for Vitest. Real tree never mutated.

| Mutation | File:line | Description | Killed? |
| -------- | --------- | ----------- | ------- |
| 1 | `SearchPage.tsx:25-29` | Removed empty-query guard (`if (!trimmed) { … return }`) | ✅ Killed — 2 tests failed (`SearchPage.test.tsx:44` `not.toHaveBeenCalled`, whitespace variant) |
| 2 | `formatMinutes.ts:13` | Changed `` `${hours}h ${minutes}min` `` → `` `${hours}h` `` | ✅ Killed — 2 tests failed (`formatMinutes.test.ts:19`; detail total display) |
| 3 | `TrackedGameDetailPage.tsx:138` | Removed `parsed <= 0` duration guard | ✅ Killed — 1 test failed (`TrackedGameDetailPage.test.tsx:287` `createSession).not.toHaveBeenCalled()`) |

**Sensor depth**: lightweight (3 mutations)
**Sensor outcome**: 3/3 killed (all mutants detected)

---

## Interactive UAT Results

Not performed in this Verifier run (automated validation only). P2 visual AC and full end-to-end demo remain UAT candidates.

| # | Test | Result | Details |
| --- | --- | --- | --- |
| - | - | ⏭️ Skip | Verifier pass — no interactive UAT this run |

---

## Code Quality

| Principle | Status |
| --------- | ------ |
| Minimum code | ✅ Thin pages + helpers; no Tailwind/Next |
| Surgical changes | ✅ Diff limited to `frontend/` + CORS |
| No scope creep | ✅ Matches ui-v1 out-of-scope |
| Matches patterns | ✅ React + Vitest + TL as designed |
| Spec-anchored outcome check (asserted values match spec) | ✅ All P1 ACs evidenced |
| Per-layer Coverage Expectation met (domain 1:1 ACs; routes happy+edge+error) | ✅ Lib/API 1:1; pages cover happy + edge/error paths |
| Every test maps to a spec requirement — no unclaimed tests | ✅ |
| Documented guidelines followed: `AGENTS.md`, `docs/stack.md`, design Testing Strategy | ✅ |

---

## Edge Cases

- [x] API unreachable → PT unreachable message — `frontend/src/api/apiClient.test.ts:54-56` - `message: 'Não foi possível alcançar a API.'`
- [x] `coverUrl` null → no broken image — `frontend/src/components/Feedback.test.tsx:10` - `queryByRole('img')).not.toBeInTheDocument()`
- [x] `rating` null → unset indication (not `0`) — `frontend/src/pages/TrackedGamesPage.test.tsx:74` - `/Sem nota/`; `TrackedGameDetailPage.test.tsx:70` - `/Sem nota/`
- [x] `totalMinutes` `0` → zero display — `frontend/src/lib/formatMinutes.test.ts:7` - `formatMinutes(0)).toBe('0 min')`; detail meta `:70` - `/0 min/`
- [x] PATCH/POST 400 shows API `message` when present — `frontend/src/api/apiClient.test.ts:44-46` - `message: 'Parâmetro q é obrigatório'`

---

## Gate Check

- **Gate command**: `cd frontend && npm test` and `cd frontend && npm run build`
- **Result**: 45 passed, 0 failed, 0 skipped; build succeeded (`tsc -b && vite build`)
- **Test count before feature**: 0 (no `frontend/` suite)
- **Test count after T15**: 22
- **Test count after T16–T19**: 42
- **Test count after T20**: 45 (+3 from T20 gap-closure tests)
- **Skipped tests**: none
- **Failures**: none

---

## T20 Gap Closure (prior FAIL → re-verified)

| Prior gap | T20 evidence | Result |
| --------- | ------------ | ------ |
| Session delete refresh UI | `TrackedGameDetailPage.test.tsx:221-223` — empty sessions + `0 min` meta after DELETE | ✅ Closed |
| 409 → `/` when no matching rawgId | `SearchPage.test.tsx:340-341` — link `Ir para meus jogos` `href='/'` | ✅ Closed |
| createTrackedGame non-409 error | `SearchPage.test.tsx:373` — alert `'Falha no servidor'` | ✅ Closed |
| listTrackedGames called on `/` | `TrackedGamesPage.test.tsx:80` — `listTrackedGames).toHaveBeenCalled()` | ✅ Closed |
| Detail cover when present | `TrackedGameDetailPage.test.tsx:84-86` — img with cover URL | ✅ Closed |

---

## Requirement Traceability Update

| Requirement | Previous Status | New Status |
| ----------- | --------------- | ---------- |
| UI-01 … UI-05 (list) | ⚠️ Partial | ✅ Verified |
| UI-06 … UI-14 (search) | ⚠️ Partial | ✅ Verified |
| UI-15 … UI-24 (detail) | ❌ Needs Fix | ✅ Verified |
| UI-25 … UI-27 (nav) | ⚠️ Partial | ✅ Verified (UI-25–26 automated); ⚠️ UAT (UI-27 visual) |

---

## Summary

**Overall**: ✅ Ready

**Spec-anchored check**: 26/27 story ACs fully matched with `file:line` evidence; 1 P2 visual ⚠️ UAT; edge cases 5/5 evidenced
**Sensor**: 3/3 mutations killed
**Gate**: 45 passed, build green

**What works**: Full ui-v1 happy path + edge/error coverage across list, search, detail, lib/API helpers, nav chrome, and cover handling. T20 closed all prior Verifier FAIL gaps.

**Remaining**: Interactive UAT for P2 minimal-diary visual (cosmetic; does not block PASS).
