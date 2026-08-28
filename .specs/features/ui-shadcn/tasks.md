# UI shadcn Tasks

**Spec**: `.specs/features/ui-shadcn/spec.md`
**Design**: `.specs/features/ui-shadcn/design.md`
**Status**: In Progress

---

## Test Coverage Matrix

> Generated from codebase, `AGENTS.md`, existing Vitest suite — confirm before Execute. Guidelines: `AGENTS.md`, `frontend/package.json` scripts, ui-v1 test patterns.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| shadcn/Tailwind scaffold | none | Build gate only | - | `cd frontend && npm run build` |
| Theme CSS variables | none | Build gate + visual | theme CSS | `npm run build` |
| TrackedGameDetailPage (Select/Card/Input) | unit (TL) | Status/rating PATCH; session create; invalid duration | `frontend/src/pages/*.test.tsx` | `cd frontend && npm test` |
| TrackedGameDetailPage (AlertDialog) | unit (TL) | Confirm/cancel session+game delete; refresh/nav | `frontend/src/pages/*.test.tsx` | `cd frontend && npm test` |
| gamesApi / lib | none (unchanged) | Existing suite must stay green | `frontend/src/**/*.test.ts` | `npm test` |

---

## Gate Check Commands

| Gate | When | Command |
| --- | --- | --- |
| Quick | After page test edits | `cd frontend && npm test` |
| Full | After detail migration | `cd frontend && npm test` |
| Build | Scaffold, theme, wiring | `cd frontend && npm run build` |

---

## Implementation Order

### Phase 1: Foundation

```
T1 -> T2 -> T3
```

### Phase 2: Detail migration

```
T3 -> T4 -> T5
T4 -> T5
```

---

## Task Breakdown

### Phase 1: Foundation

### T1: Add Tailwind CSS to Vite frontend

**What**: Install and configure Tailwind for the existing Vite React TS app (official Vite path / Context7).
**Where**: `frontend/vite.config.ts`, CSS entry, `package.json`
**Depends on**: None
**Reuses**: Existing Vite app
**Requirement**: SHAD-01, SHAD-03

**Tools**:

- MCP: `user-context7` (Tailwind Vite + shadcn)
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Tailwind configured and imported
- [x] `npm run build` succeeds

**Tests**: none
**Gate**: build

**Commit**: `build(ui): add Tailwind CSS to Vite frontend`

---

### T2: Initialize shadcn and Letterboxd dark theme tokens

**What**: Run shadcn init (`components.json`, `cn` util, base CSS variables). Map dark canvas + green primary to AD-012. Force dark as default (e.g. `class="dark"` on root).
**Where**: `frontend/components.json`, `frontend/src/lib/utils.ts`, theme CSS, `index.html` / `main.tsx`
**Depends on**: T1
**Reuses**: AD-012 colors
**Requirement**: SHAD-02, SHAD-04, SHAD-17, SHAD-18

**Tools**:

- MCP: `user-context7`
- Skill: `tlc-spec-driven`

**Done when**:

- [x] `components.json` present
- [x] Theme variables set for dark diary + green accent
- [x] Default appearance is dark-only
- [x] `npm run build` succeeds

**Tests**: none
**Gate**: build

**Commit**: `style(ui): init shadcn with dark Letterboxd tokens`

---

### T3: Add shadcn primitives via CLI

**What**: `npx shadcn@latest add button card input select alert-dialog` (non-interactive flags as supported).
**Where**: `frontend/src/components/ui/`
**Depends on**: T2
**Reuses**: shadcn CLI
**Requirement**: SHAD-02

**Tools**:

- MCP: `user-context7`
- Skill: `tlc-spec-driven`

**Done when**:

- [x] button, card, input, select, alert-dialog files exist under `components/ui/`
- [x] `npm run build` succeeds

**Tests**: none
**Gate**: build

**Commit**: `feat(ui): add shadcn button card input select alert-dialog`

---

### Phase 2: Detail migration

### T4: Migrate detail Progresso and Sessões to Card/Select/Input/Button

**What**: Rewrite detail log panels to use Card, Select (status/rating), Input, Button. Keep handlers, PT labels, validation message for invalid duration.
**Where**: `frontend/src/pages/TrackedGameDetailPage.tsx`, `TrackedGameDetailPage.test.tsx`
**Depends on**: T3
**Reuses**: gamesApi, playStatus, formatMinutes, Feedback error/cover
**Requirement**: SHAD-05 … SHAD-10

**Tools**:

- MCP: `user-context7` (Select controlled API)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Progresso/Sessões rendered in Cards
- [ ] Status/rating Select trigger PATCH + UI refresh (tested)
- [ ] Session create >0 works; ≤0 shows PT validation without API (tested)
- [ ] Gate: `npm test` green

**Tests**: unit
**Gate**: full

**Commit**: `feat(ui): migrate detail log panels to shadcn controls`

---

### T5: Replace window.confirm with AlertDialog for deletes

**What**: Session Remover and Remover jogo use AlertDialog; confirm/cancel behaviors per spec; keep refresh/nav outcomes.
**Where**: `frontend/src/pages/TrackedGameDetailPage.tsx`, `TrackedGameDetailPage.test.tsx`
**Depends on**: T4
**Reuses**: deleteSession, deleteTrackedGame
**Requirement**: SHAD-11 … SHAD-16

**Tools**:

- MCP: `user-context7` (AlertDialog)
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] No `window.confirm` on detail deletes
- [ ] Cancel → no DELETE (tested)
- [ ] Confirm session delete → DELETE + refresh (tested)
- [ ] Confirm game delete → DELETE + navigate `/` (tested)
- [ ] Gate: `npm test` and `npm run build` green

**Tests**: unit
**Gate**: full

**Commit**: `feat(ui): use AlertDialog for detail delete confirms`

---

## Parallelization Map

```
Phase 1 -> Phase 2

Phase 1:  T1 -> T2 -> T3
Phase 2:  T3 -> T4 -> T5
```

Preferred inline order: T1, T2, T3, T4, T5.

**Batches at Execute:** Single batch (5 tasks ≤ ~8) — execute inline; no sub-agent offer required.

---

## Execution Plan

```
Phase 1 (Foundation) → Phase 2 (Detail migration)
T1 → T2 → T3 → T4 → T5
```

| Order | Task | Notes |
| --- | --- | --- |
| 1 | T1 | Tailwind on Vite |
| 2 | T2 | shadcn init + dark tokens |
| 3 | T3 | add UI primitives |
| 4 | T4 | migrate detail controls |
| 5 | T5 | AlertDialog deletes |

---

## Task Granularity Check

| Task | Scope | Status |
| --- | --- | --- |
| T1 Tailwind | tooling config | OK |
| T2 shadcn init + theme | config + tokens | OK |
| T3 add primitives | CLI components | OK |
| T4 detail controls | one page + tests | OK |
| T5 AlertDialog deletes | one page + tests | OK |

---

## Diagram-Definition Cross-Check

| Task | Depends On | Diagram | Status |
| --- | --- | --- | --- |
| T1 | None | root | OK |
| T2 | T1 | T1 -> T2 | OK |
| T3 | T2 | T2 -> T3 | OK |
| T4 | T3 | T3 -> T4 | OK |
| T5 | T4 | T4 -> T5 | OK |

---

## Test Co-location Validation

| Task | Layer | Matrix | Task Says | Status |
| --- | --- | --- | --- | --- |
| T1 | scaffold | none | none | OK |
| T2 | theme | none | none | OK |
| T3 | ui primitives | none | none | OK |
| T4 | pages | unit | unit | OK |
| T5 | pages | unit | unit | OK |

---

## Requirement Traceability Update

| ID | Task |
| --- | --- |
| SHAD-01, SHAD-03 | T1 |
| SHAD-02, SHAD-04, SHAD-17, SHAD-18 | T2 |
| SHAD-02 | T3 |
| SHAD-05…SHAD-10 | T4 |
| SHAD-11…SHAD-16 | T5 |
