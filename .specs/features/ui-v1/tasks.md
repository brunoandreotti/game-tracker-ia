# UI v1 Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user - do not proceed without it.**

**Code gate:** Do not start Execute until Bruno explicitly allows generating code (e.g. “pode implementar” / “pode gerar o código”).

---

**Design**: `.specs/features/ui-v1/design.md`
**Status**: Implementing

---

## Test Coverage Matrix

> Generated from codebase, project guidelines, and spec - confirm before Execute. Guidelines found: `AGENTS.md`, `docs/stack.md`, `.specs/features/ui-v1/design.md` (Testing Strategy). No `frontend/` tests yet — matrix targets the design.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Lib helpers (`formatMinutes`, `playStatus`) | unit | Branches of format + all four status labels | `frontend/src/**/*.test.ts` | `npm test` (in `frontend/`) |
| API client (`apiClient`, `gamesApi`) | unit | Happy JSON; error body `message`; network failure; 204 DELETE | `frontend/src/**/*.test.ts` | `npm test` (in `frontend/`) |
| Pages (lista empty; busca q vazio) | unit (Testing Library) | Empty state CTA; empty query does not call API | `frontend/src/**/*.test.tsx` | `npm test` (in `frontend/`) |
| Spring CORS config | none | Build gate only | - | `mvn -q package` |
| Vite scaffold / CSS / layout chrome | none | Build gate (`npm run build`) | - | `npm run build` (in `frontend/`) |

## Gate Check Commands

> Generated from design + repo tools - confirm before Execute. After scaffold exists:

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | After front unit/component tests | `cd frontend && npm test` |
| Full | After page tests or API client suite | `cd frontend && npm test` |
| Build | Scaffold, CSS, CORS, wiring | `cd frontend && npm run build` and/or `mvn -q package` (CORS tasks) |

---

## Execution Plan

Phases are ordered and run sequentially - each phase completes before the next begins, and tasks within a phase execute in order.

### Phase 1: Foundation

```
T1 -> T2 -> T4 -> T5 -> T6
T1 -> T3
```

### Phase 2: Shared UI + lista

```
T7 -> T12
T8 -> T12
T9 -> T10 -> T12
T9 -> T11 -> T12
```

### Phase 3: Busca + detalhe + wiring

```
T12 -> T15
T13 -> T15
T14 -> T15
```---

## Task Breakdown

### Phase 1: Foundation

### T1: Scaffold Vite React TypeScript app in frontend/

**What**: Create `frontend/` with Vite + React + TypeScript template, Vitest + Testing Library scripts, and a green `npm test` / `npm run build` baseline (replace default App demo later).
**Where**: `frontend/package.json`
**Depends on**: None
**Reuses**: AD-010 layout; Context7 for current Vite React-TS scaffold
**Requirement**: UI-01

**Tools**:

- MCP: `user-context7` (Vite React TypeScript scaffold, Vitest)
- Skill: `tlc-spec-driven`

**Done when**:

- [x] `frontend/` exists with React + TS + Vite
- [x] `npm install` succeeds
- [x] Gate check passes: `cd frontend && npm run build`
- [x] `npm test` runs (may be empty suite or placeholder)

**Tests**: none
**Gate**: build

**Commit**: `build(ui): scaffold Vite React TypeScript app`

---

### T2: Add VITE_API_URL env typing and example

**What**: Add `.env.example` with `VITE_API_URL=http://localhost:8080` and augment `ImportMetaEnv` in `vite-env.d.ts`.
**Where**: `frontend/.env.example`
**Depends on**: T1
**Reuses**: Vite env docs (prefix `VITE_`)
**Requirement**: UI-01

**Tools**:

- MCP: `user-context7` (Vite env + ImportMetaEnv)
- Skill: `tlc-spec-driven`

**Done when**:

- [x] `.env.example` documents `VITE_API_URL`
- [x] `vite-env.d.ts` types `VITE_API_URL`
- [x] No secrets committed
- [x] Gate check passes: `cd frontend && npm run build`

**Tests**: none
**Gate**: build

**Commit**: `chore(ui): add VITE_API_URL example and types`

---

### T3: Add Spring CORS for Vite origin

**What**: Add `CorsConfig` implementing `WebMvcConfigurer` and `app.cors.allowed-origins` in YAML (default `http://localhost:5173`), mapping API methods used by the UI.
**Where**: `src/main/java/com/brunoandreotti/game_tracker/config/CorsConfig.java`
**Depends on**: T1
**Reuses**: Existing `config/` package; Spring CORS reference
**Requirement**: UI-01

**Tools**:

- MCP: `user-context7` (Spring WebMvcConfigurer CORS)
- Skill: `tlc-spec-driven`

**Done when**:

- [x] `CorsConfig` registers allowed origins from config
- [x] `application.yaml` has `app.cors.allowed-origins`
- [x] Gate check passes: `mvn -q package`
- [x] Existing tests still green (no silent deletions)

**Tests**: none
**Gate**: build

**Commit**: `feat(api): allow CORS from Vite dev origin`

---

### T4: Add API DTO types

**What**: Define TypeScript DTOs (`PlayStatus`, `GameSummaryDto`, `TrackedGameDto`, `SessionDto`, `ApiErrorBody`) matching the API contract.
**Where**: `frontend/src/api/types.ts`
**Depends on**: T2
**Reuses**: `docs/product.md` JSON shapes
**Requirement**: UI-01

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Types match design.md Data Models
- [x] Exported for use by `apiClient` / pages
- [x] Gate check passes: `cd frontend && npm run build`

**Tests**: none
**Gate**: build

**Commit**: `feat(ui): add API DTO types`

---

### T5: Implement apiClient with error parsing

**What**: Implement `apiRequest` using `fetch` + `VITE_API_URL`, parse JSON error `message`, map network failures to Portuguese message, export `ApiError`.
**Where**: `frontend/src/api/apiClient.ts`
**Depends on**: T4
**Reuses**: Design error table
**Requirement**: UI-04

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Happy path returns parsed JSON
- [x] Non-OK with body uses `message`
- [x] Network error → PT message about API unreachable
- [x] Unit tests cover happy, body error, network failure
- [x] Gate check passes: `cd frontend && npm test`
- [x] Test count: at least 3 tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ui): add fetch apiClient with ApiError`

---

### T6: Implement gamesApi endpoint helpers

**What**: Wrap all UI endpoints (search, tracked CRUD, sessions) as named functions on top of `apiClient`.
**Where**: `frontend/src/api/gamesApi.ts`
**Depends on**: T5
**Reuses**: `apiClient`, Bruno collection paths
**Requirement**: UI-07

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [x] All methods from design `gamesApi` exist
- [x] `createTrackedGame` sends only `{ rawgId }`
- [x] DELETE helpers tolerate empty 204 body
- [x] Unit tests with mocked fetch cover search query params and one mutate path
- [x] Gate check passes: `cd frontend && npm test`
- [x] Test count: prior tests + new ones pass (no silent deletions)

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ui): add gamesApi endpoint helpers`

---

### Phase 2: Shared UI + lista

### T7: Add formatMinutes helper

**What**: Implement `formatMinutes(totalMinutes)` per design (`0 min`, `45 min`, `1h`, `1h 30min`).
**Where**: `frontend/src/lib/formatMinutes.ts`
**Depends on**: T1
**Reuses**: Design formatMinutes rules
**Requirement**: UI-01

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Function matches design cases
- [x] Unit tests cover 0, minutes-only, hours-only, mixed
- [x] Gate check passes: `cd frontend && npm test`

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ui): format totalMinutes for display`

---

### T8: Add playStatus PT labels

**What**: Map API `PlayStatus` enums to Portuguese labels and export options list for selects.
**Where**: `frontend/src/lib/playStatus.ts`
**Depends on**: T4
**Reuses**: Design label table
**Requirement**: UI-01

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [x] All four statuses have PT labels
- [x] Unit tests assert the four mappings
- [x] Gate check passes: `cd frontend && npm test`

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ui): map play status to Portuguese labels`

---

### T9: Add global diary-minimal CSS

**What**: Create `global.css` with CSS variables, restrained typography, modest spacing; import from `main.tsx`.
**Where**: `frontend/src/styles/global.css`
**Depends on**: T1
**Reuses**: Design “diário minimal”
**Requirement**: UI-27

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Variables define text/bg/accent without store-like chrome
- [x] Imported once at app entry
- [x] Gate check passes: `cd frontend && npm run build`

**Tests**: none
**Gate**: build

**Commit**: `style(ui): add diary-minimal global CSS`

---

### T10: Add shared feedback and cover components

**What**: Add `LoadingMessage`, `ErrorMessage`, and `CoverImage` (omit/hide on null or load error) in one feedback module.
**Where**: `frontend/src/components/Feedback.tsx`
**Depends on**: T9
**Reuses**: Design components section
**Requirement**: UI-05

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Loading and error messages render text via props
- [x] CoverImage does not show broken image for null URL
- [x] Gate check passes: `cd frontend && npm run build`

**Tests**: none
**Gate**: build

**Commit**: `feat(ui): add loading error and cover components`

---

### T11: Add AppLayout chrome

**What**: Layout with nav links “Meus jogos” and “Buscar” and `<Outlet />` for child routes.
**Where**: `frontend/src/components/AppLayout.tsx`
**Depends on**: T9
**Reuses**: React Router `Link` / `Outlet` (library mode)
**Requirement**: UI-25

**Tools**:

- MCP: `user-context7` (React Router BrowserRouter layout outlet)
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Nav links to `/` and `/search` in Portuguese
- [x] Renders child route via Outlet
- [x] Gate check passes: `cd frontend && npm run build`

**Tests**: none
**Gate**: build

**Commit**: `feat(ui): add AppLayout navigation chrome`

---

### T12: Implement TrackedGamesPage list

**What**: Home page loads tracked games, shows loading/error/empty (CTA to search), lists name/status/rating/time, navigates to detail on activate.
**Where**: `frontend/src/pages/TrackedGamesPage.tsx`
**Depends on**: T6, T7, T8, T10, T11
**Reuses**: `gamesApi.listTrackedGames`, feedback components
**Requirement**: UI-01

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Fetches on mount; loading and error states work
- [x] Empty state links to `/search`
- [x] List shows PT status + formatted minutes + unset rating indication
- [x] Testing Library: empty state visible when API returns `[]`
- [x] Gate check passes: `cd frontend && npm test`

**Tests**: unit
**Gate**: full

**Commit**: `feat(ui): implement tracked games list page`

---

### Phase 3: Busca + detalhe + wiring

### T13: Implement SearchPage

**What**: Search form with exact checkbox, results, track action then navigate to detail, 409 handling via list lookup, local validation for empty `q`, anti double-submit while tracking.
**Where**: `frontend/src/pages/SearchPage.tsx`
**Depends on**: T6, T10, T11
**Reuses**: `gamesApi.searchGames`, `createTrackedGame`, `listTrackedGames`
**Requirement**: UI-06

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Exact checkbox maps to `exact=true`
- [x] Empty/whitespace query does not call API (tested)
- [x] Successful track navigates to `/games/{id}`
- [x] 409 shows PT message + link to existing game or `/`
- [x] Gate check passes: `cd frontend && npm test`

**Tests**: unit
**Gate**: full

**Commit**: `feat(ui): implement search and track page`

---

### T14: Implement TrackedGameDetailPage

**What**: Detail loads game + sessions; patch status/rating; create session with validation; delete session/game with `window.confirm`; 404 state; back link to `/`.
**Where**: `frontend/src/pages/TrackedGameDetailPage.tsx`
**Depends on**: T6, T7, T8, T10, T11
**Reuses**: `gamesApi` mutators; design confirm rule
**Requirement**: UI-15

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Loads game and sessions for `:id`
- [x] Status/rating PATCH refresh UI
- [x] Session create/delete refresh totals; invalid duration blocked client-side
- [x] Deletes require confirm; delete game navigates to `/`
- [x] 404 shows PT message + link home
- [x] At least one test for invalid duration (no API call)
- [x] Gate check passes: `cd frontend && npm test`

**Tests**: unit
**Gate**: full

**Commit**: `feat(ui): implement tracked game detail page`

---

### T15: Wire routes in App and main

**What**: `BrowserRouter` routes: layout + `/` list, `/search`, `/games/:id`; mount from `main.tsx`; remove Vite demo chrome.
**Where**: `frontend/src/App.tsx`
**Depends on**: T12, T13, T14
**Reuses**: React Router library mode from design
**Requirement**: UI-25

**Tools**:

- MCP: `user-context7` (React Router Routes)
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Three routes work under AppLayout
- [x] Default Vite counter/demo removed
- [x] Gate check passes: `cd frontend && npm run build` and `cd frontend && npm test`

**Tests**: none
**Gate**: build

**Commit**: `feat(ui): wire app routes for list search and detail`

---

### Phase 4: Test coverage fixes (validation gaps)

### T16: Fix TrackedGamesPage ACs

**What**: Add tests for list happy path (two games), ApiError alert, and loading state.
**Where**: `frontend/src/pages/TrackedGamesPage.test.tsx`
**Depends on**: T12
**Status**: Done

**Done when**:

- [x] Two games show names, PT status, rating labels, formatted minutes, detail links
- [x] ApiError shows alert message; no list heading or fabricated data
- [x] Deferred load shows `Carregando jogos...`
- [x] Gate check passes: `cd frontend && npm test`

**Commit**: `test(ui): cover tracked games list happy error loading`

---

### T17: Fix SearchPage ACs

**What**: Add tests for results display, empty results, ApiError, loading state, and optional tracking disabled state.
**Where**: `frontend/src/pages/SearchPage.test.tsx`
**Depends on**: T13
**Status**: Implementing

**Done when**:

- [ ] Results show name, year, cover img when coverUrl set
- [ ] Empty search results show `Nenhum jogo encontrado.`
- [ ] searchGames ApiError shows API message
- [ ] Loading shows `Buscando jogos...` and disables Buscar button
- [ ] Existing empty-query, exact, track nav, 409 tests remain
- [ ] Gate check passes: `cd frontend && npm test`

**Commit**: `test(ui): cover search results empty error loading`

---

### T18: Fix TrackedGameDetailPage mutate ACs

**What**: Add tests for load display, status/rating patch, session create, delete confirms, delete game navigate, patch failure.
**Where**: `frontend/src/pages/TrackedGameDetailPage.test.tsx`
**Depends on**: T14
**Status**: Implementing

**Done when**:

- [ ] Load shows heading, year, status, rating, minutes, sessions empty text
- [ ] Status change calls patchTrackedGame and updates UI
- [ ] Rating change calls patchTrackedGame and updates UI
- [ ] Session create calls createSession and refreshes totals
- [ ] deleteSession respects window.confirm true/false
- [ ] deleteTrackedGame + navigate `/` when confirm true
- [ ] Patch failure shows error; previous game data remains
- [ ] Existing duration≤0 and 404 tests remain
- [ ] Gate check passes: `cd frontend && npm test`

**Commit**: `test(ui): cover detail status rating sessions deletes`

---

### T19: Fix AppLayout + CoverImage

**What**: Add tests for nav links and CoverImage null/src behavior.
**Where**: `frontend/src/components/AppLayout.test.tsx`, `frontend/src/components/Feedback.test.tsx`
**Depends on**: T10, T11
**Status**: Implementing

**Done when**:

- [ ] AppLayout nav links: Meus jogos → `/`, Buscar → `/search`
- [ ] CoverImage null src → no img; url src → img with alt
- [ ] Gate check passes: `cd frontend && npm test`

**Commit**: `test(ui): cover app nav and null cover image`

---

## Phase Execution Map

```
Phase 1 -> Phase 2 -> Phase 3 -> Phase 4

Phase 1:  T1 -> T2 -> T4 -> T5 -> T6
          T1 -> T3
Phase 2:  T7 -> T12
          T8 -> T12
          T9 -> T10 -> T12
          T9 -> T11 -> T12
Phase 3:  T12 -> T15
          T13 -> T15
          T14 -> T15
Phase 4:  T16 -> T17 -> T18 -> T19
```

Preferred inline order: T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19.

**Batches at Execute (~7 tasks):** Phase 1 (T1–T6) | Phase 2 (T7–T12) | Phase 3 (T13–T15) | Phase 4 (T16–T19). Offer sub-agents; do not auto-spawn. **No code until Bruno allows Execute.**
---

## Task Granularity Check

| Task | Scope | Status |
| --- | --- | --- |
| T1: Scaffold Vite app | package + scaffold | OK (one scaffold deliverable) |
| T2: Env example + types | env config | OK |
| T3: CorsConfig | one config class | OK |
| T4: DTO types | one types file | OK |
| T5: apiClient | one module + tests | OK |
| T6: gamesApi | one module + tests | OK |
| T7: formatMinutes | one helper + tests | OK |
| T8: playStatus | one helper + tests | OK |
| T9: global.css | one stylesheet | OK |
| T10: Feedback components | one cohesive module | OK |
| T11: AppLayout | one component | OK |
| T12: TrackedGamesPage | one page + tests | OK |
| T13: SearchPage | one page + tests | OK |
| T14: DetailPage | one page + tests | OK |
| T15: App routes | one wiring file | OK |

---

## Diagram-Definition Cross-Check

| Task | Depends On (task body) | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | (root) | OK |
| T2 | T1 | T1 -> T2 | OK |
| T3 | T1 | T1 -> T3 | OK |
| T4 | T2 | T2 -> T4 | OK |
| T5 | T4 | T4 -> T5 | OK |
| T6 | T5 | T5 -> T6 | OK |
| T7 | T1 | cross-phase (no Phase 2 root arrow) | OK |
| T8 | T4 | cross-phase | OK |
| T9 | T1 | cross-phase | OK |
| T10 | T9 | T9 -> T10 | OK |
| T11 | T9 | T9 -> T11 | OK |
| T12 | T6, T7, T8, T10, T11 | T7/T8/T10/T11 -> T12 | OK |
| T13 | T6, T10, T11 | cross-phase + T13 -> T15 | OK |
| T14 | T6, T7, T8, T10, T11 | T14 -> T15 | OK |
| T15 | T12, T13, T14 | T12/T13/T14 -> T15 | OK |

Cross-phase `Depends on` (T1/T4/T6) are satisfied when the later phase starts; they are not redrawn as Phase 2/3 edges.

---

## Test Co-location Validation

| Task | Code Layer Created/Modified | Matrix Requires | Task Says | Status |
| --- | --- | --- | --- | --- |
| T1 | Vite scaffold | none | none | OK |
| T2 | Env / types ambient | none | none | OK |
| T3 | Spring CORS config | none | none | OK |
| T4 | DTO types | none | none | OK |
| T5 | API client | unit | unit | OK |
| T6 | API client | unit | unit | OK |
| T7 | Lib helpers | unit | unit | OK |
| T8 | Lib helpers | unit | unit | OK |
| T9 | CSS | none | none | OK |
| T10 | Layout chrome / presentational | none | none | OK |
| T11 | Layout chrome | none | none | OK |
| T12 | Pages | unit (Testing Library) | unit | OK |
| T13 | Pages | unit (Testing Library) | unit | OK |
| T14 | Pages | unit (Testing Library) | unit | OK |
| T15 | Route wiring | none | none | OK |

---

## Requirement Traceability Update

After approval, map in `spec.md`: UI-01…UI-27 → In Tasks (covered by T1–T15; page tasks cover the P1 AC clusters).
