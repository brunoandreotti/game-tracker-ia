# UI shadcn Validation

**Date**: 2026-08-28
**Spec**: `.specs/features/ui-shadcn/spec.md`
**Diff range**: `b4d4993^..HEAD` (T1–T5 shadcn commits)
**Verifier**: independent sub-agent (author ≠ verifier)

---

## Task Completion

| Task | Status  | Notes |
| ---- | ------- | ----- |
| T1   | ✅ Done | Tailwind on Vite |
| T2   | ✅ Done | shadcn init + dark tokens |
| T3   | ✅ Done | button, card, input, select, alert-dialog |
| T4   | ✅ Done | Card/Select/Input/Button on detail |
| T5   | ✅ Done | AlertDialog deletes |

---

## Spec-Anchored Acceptance Criteria

### P1: Scaffold Tailwind + shadcn

| Criterion (WHEN X THEN Y) | Spec-defined outcome | `file:line` + assertion | Result |
| ------------------------- | -------------------- | ----------------------- | ------ |
| SHALL include Tailwind CSS configured for Vite React TS app | `@tailwindcss/vite` plugin wired | `frontend/vite.config.ts:8` - `plugins: [react(), tailwindcss()]` | ✅ PASS |
| SHALL include shadcn `components.json` and base utilities | components.json + `cn` helper | `frontend/components.json:1` - schema present; `frontend/src/lib/utils.ts:4` - `export function cn(...)` | ✅ PASS |
| WHEN `npm run build` THEN build SHALL succeed with Tailwind + shadcn | exit 0, dist assets emitted | Gate run 2026-08-28 — `npm run build` exit 0, `dist/assets/index-I5aPfXui.css` 46.67 kB | ✅ PASS |
| SHALL keep dark-only theming as default | no light scheme required | `frontend/index.html:2` - `class="dark"`; `frontend/src/styles/tailwind.css:51` - `:root, .dark` single scheme | ✅ PASS |

### P1: Detalhe com Select, Card e Input

| Criterion (WHEN X THEN Y) | Spec-defined outcome | `file:line` + assertion | Result |
| ------------------------- | -------------------- | ----------------------- | ------ |
| WHEN user opens `/games/{id}` THEN Progresso and Sessões in Card panels | Card sections titled Progresso / Sessões | `frontend/src/pages/TrackedGameDetailPage.tsx:268` - `<CardTitle>Progresso</CardTitle>`; `:329` - `<CardTitle>Sessões</CardTitle>` | ✅ PASS |
| WHEN status changed THEN PATCH with status and refresh PT label | `PATCH { status: 'COMPLETED' }`, UI shows "Zerei" | `frontend/src/pages/TrackedGameDetailPage.test.tsx:123` - `expect(gamesApi.patchTrackedGame).toHaveBeenCalledWith(1, { status: 'COMPLETED' })`; `:124` - `expect(screen.getByText(/2017 · Zerei · Sem nota · 0 min/))` | ✅ PASS |
| WHEN rating 1–10 set THEN PATCH with rating and refresh | `PATCH { rating: 9 }`, UI shows "Nota 9" | `frontend/src/pages/TrackedGameDetailPage.test.tsx:147` - `expect(gamesApi.patchTrackedGame).toHaveBeenCalledWith(1, { rating: 9 })`; `:148` - `expect(screen.getByText(/Nota 9/))` | ✅ PASS |
| WHEN session submitted with durationMinutes > 0 THEN POST and refresh | `POST { durationMinutes: 90 }`, total updates | `frontend/src/pages/TrackedGameDetailPage.test.tsx:176` - `expect(gamesApi.createSession).toHaveBeenCalledWith(1, { durationMinutes: 90 })`; `:177` - `expect(screen.getByText(/1h 30min/))` | ✅ PASS |
| IF duration ≤ 0 or non-numeric THEN no API + PT validation message | no createSession; message "Informe uma duração em minutos maior que zero." | `frontend/src/pages/TrackedGameDetailPage.test.tsx:313` - `expect(gamesApi.createSession).not.toHaveBeenCalled()`; `:314-315` - PT alert text; `frontend/src/pages/TrackedGameDetailPage.tsx:166` - `Number.isNaN(parsed)` branch | ⚠️ Spec-precision gap (zero covered; non-numeric branch impl-only) |
| SHALL preserve PT labels and accessible names | Status, Nota, Duração (min), Registrar sessão | `frontend/src/pages/TrackedGameDetailPage.test.tsx:119` - `getByRole('combobox', { name: 'Status' })`; `:143` - Nota combobox; `:172` - Duração label; `:173` - Registrar sessão button | ✅ PASS |

### P1: Confirmações com AlertDialog

| Criterion (WHEN X THEN Y) | Spec-defined outcome | `file:line` + assertion | Result |
| ------------------------- | -------------------- | ----------------------- | ------ |
| WHEN Remover on session THEN AlertDialog before DELETE | dialog visible, no immediate DELETE | `frontend/src/pages/TrackedGameDetailPage.test.tsx:196` - `expect(await screen.findByRole('alertdialog')).toBeInTheDocument()`; `:197` - `'Remover esta sessão?'` | ✅ PASS |
| WHEN confirm session delete THEN DELETE and refresh | `deleteSession(1, 10)`, empty list + total 0 | `frontend/src/pages/TrackedGameDetailPage.test.tsx:228` - `expect(gamesApi.deleteSession).toHaveBeenCalledWith(1, 10)`; `:229-230` - refreshed UI | ✅ PASS |
| WHEN cancel session delete THEN no DELETE | dialog closes, deleteSession not called | `frontend/src/pages/TrackedGameDetailPage.test.tsx:202` - `expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()`; `:204` - `expect(gamesApi.deleteSession).not.toHaveBeenCalled()` | ✅ PASS |
| WHEN Remover jogo THEN AlertDialog before DELETE | dialog with PT title | `frontend/src/pages/TrackedGameDetailPage.test.tsx:247` - `expect(await screen.findByText('Remover este jogo da sua lista?'))` | ✅ PASS |
| WHEN confirm game delete THEN DELETE and navigate `/` | `deleteTrackedGame(1)`, navigate home | `frontend/src/pages/TrackedGameDetailPage.test.tsx:251` - `expect(gamesApi.deleteTrackedGame).toHaveBeenCalledWith(1)`; `:252` - `expect(navigateMock).toHaveBeenCalledWith('/')` | ✅ PASS |
| WHEN cancel game delete THEN no DELETE, stay on page | no deleteTrackedGame, dialog dismissed | `frontend/src/pages/TrackedGameDetailPage.test.tsx:271` - `expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()`; `:273` - `expect(gamesApi.deleteTrackedGame).not.toHaveBeenCalled()` | ✅ PASS |

### P2: Tema Letterboxd nos tokens shadcn

| Criterion (WHEN X THEN Y) | Spec-defined outcome | `file:line` + assertion | Result |
| ------------------------- | -------------------- | ----------------------- | ------ |
| SHALL map theme vars: canvas ~`#12161a`, primary ~`#00c030` | CSS variables on :root/.dark | `frontend/src/styles/tailwind.css:53` - `--background: #12161a`; `:59` - `--primary: #00c030` | ⚠️ UAT (tokens present; visual review not run by Verifier) |
| SHALL NOT require light theme toggle | dark-only default | `frontend/index.html:2` - `class="dark"` only; no toggle in detail page | ✅ PASS |

**Status**: ✅ All P1 ACs evidenced — 1 spec-precision gap (non-numeric duration), P2 visual token AC deferred to UAT

---

## Discrimination Sensor

Scratch worktree: `../game-tracker-ia-sensor-scratch` @ HEAD (mutations only in scratch; real tree untouched). Baseline porcelain captured before sensor; matched after (`git status --porcelain` unchanged).

| Mutation | File:line | Description | Killed? |
| -------- | --------- | ----------- | ------- |
| 1 | `frontend/src/pages/TrackedGameDetailPage.tsx:166` | Flipped `parsed <= 0` → `parsed < 0` | ✅ Killed — invalid duration test failed (`createSession` called with 0) |
| 2 | `frontend/src/pages/TrackedGameDetailPage.tsx:200` | Removed `deleteSession` call in confirm path | ✅ Killed — confirm session delete test failed (0 calls) |
| 3 | `frontend/src/pages/TrackedGameDetailPage.tsx:214` | Changed `navigate('/')` → `navigate('/search')` | ✅ Killed — game delete navigate test failed |

**Sensor depth**: lightweight (3 mutations)
**Sensor outcome**: 3/3 killed — PASS ✅

---

## Interactive UAT Results

Not performed by Verifier. P2 visual theme (SHAD-17 canvas/accent appearance) flagged ⚠️ UAT for Bruno.

---

## Code Quality

| Principle        | Status |
| ---------------- | ------ |
| Minimum code     | ✅ — scope limited to detail page + scaffold |
| Surgical changes | ✅ — list/search/nav unchanged |
| No scope creep   | ✅ — no API contract changes |
| Matches patterns | ✅ — ui-v1 handlers/PT text preserved |
| Spec-anchored outcome check | ✅ — assertions target spec values |
| Per-layer Coverage Expectation | ✅ — detail unit tests cover mutating flows |
| Every test maps to spec requirement | ✅ — 13 detail tests map to ui-v1 + shadcn ACs |
| Documented guidelines: `AGENTS.md`, tasks matrix | ✅ |

---

## Edge Cases

- [x] 404 on GET: `frontend/src/pages/TrackedGameDetailPage.test.tsx:326` - `'Jogo não encontrado.'`
- [x] Mutating request fails: `frontend/src/pages/TrackedGameDetailPage.test.tsx:294` - PT error alert, heading remains
- [ ] AlertDialog Escape dismiss: no automated test — Radix default; ⚠️ manual UAT optional

---

## Gate Check

- **Gate command**: `cd frontend && npm test` and `npm run build`
- **Result**: 46 passed, 0 failed, 0 skipped (9 files); build exit 0
- **Test count before feature** (detail page): 12 tests at `b4d4993^`
- **Test count after feature** (detail page): 13 tests
- **Delta**: +1 detail test (game delete cancel)
- **Skipped tests**: none
- **Failures**: none
- **window.confirm**: absent from `TrackedGameDetailPage.tsx` (grep clean)

---

## Requirement Traceability Update

| Requirement | Previous Status | New Status   |
| ----------- | --------------- | ------------ |
| SHAD-01     | Pending         | ✅ Verified  |
| SHAD-02     | Pending         | ✅ Verified  |
| SHAD-03     | Pending         | ✅ Verified  |
| SHAD-04     | Pending         | ✅ Verified  |
| SHAD-05     | Pending         | ✅ Verified  |
| SHAD-06     | Pending         | ✅ Verified  |
| SHAD-07     | Pending         | ✅ Verified  |
| SHAD-08     | Pending         | ✅ Verified  |
| SHAD-09     | Pending         | ⚠️ Partial (non-numeric branch untested) |
| SHAD-10     | Pending         | ✅ Verified  |
| SHAD-11     | Pending         | ✅ Verified  |
| SHAD-12     | Pending         | ✅ Verified  |
| SHAD-13     | Pending         | ✅ Verified  |
| SHAD-14     | Pending         | ✅ Verified  |
| SHAD-15     | Pending         | ✅ Verified  |
| SHAD-16     | Pending         | ✅ Verified  |
| SHAD-17     | Pending         | ⚠️ UAT (tokens verified in CSS) |
| SHAD-18     | Pending         | ✅ Verified  |

---

## Summary

**Overall**: ✅ Ready

**Spec-anchored check**: 16/18 ACs fully evidenced; 1 spec-precision gap (non-numeric duration); 1 P2 visual AC for UAT
**Sensor**: 3/3 mutations killed
**Gate**: 46 passed, build OK

**What works**: Tailwind + shadcn scaffold; detail page Card/Select/Input/Button; AlertDialog delete flows; PT labels and API behavior preserved; tests discriminate regressions.

**Issues found**: Minor — add non-numeric duration test if desired; Bruno visual UAT for Letterboxd look on detail.

**Next steps**: Optional UAT on detail page theme; optional test for non-numeric duration and Escape dismiss.

---

## Validation

**Result**: PASS
