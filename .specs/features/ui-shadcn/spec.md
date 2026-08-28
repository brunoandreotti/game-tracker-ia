# UI shadcn (detail) Specification

## Problem Statement

A UI v1 já cobre o happy path com CSS próprio e tema dark (AD-012), mas Bruno quer estudar um framework de componentes. shadcn/ui + Tailwind (AD-013) entra no projeto de estudo sem mudar o contrato da API. O primeiro slice aplica shadcn na página de detalhe (progresso, sessões, confirmações), onde o formulário ainda parece “nativo demais”.

## Goals

- [ ] Tailwind + shadcn inicializados em `frontend/` com tema dark alinhado a AD-012
- [ ] Página `/games/:id` usa componentes shadcn (Card, Select, Input, Button, AlertDialog) mantendo comportamento e textos PT do ui-v1
- [ ] Testes Vitest da detail page continuam verdes (asserts de comportamento, não de CSS)

## Out of Scope

| Feature | Reason |
| --- | --- |
| Migrar lista `/` e busca `/search` para shadcn nesta feature | Slice 1 = detalhe; lista/busca na próxima |
| antd / MUI / Mantine | AD-013 escolheu shadcn |
| Toggle light/dark | Dark-only (AD-012) |
| Mudar contrato HTTP / `gamesApi` | Só apresentação |
| Next.js / SSR | SPA Vite permanece |
| Remover 100% do `global.css` nesta feature | Coexistência na migração; limpeza depois |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Stack de componentes | shadcn/ui + Tailwind (Vite) | AD-013; estudo | y |
| Escopo de páginas | Só `TrackedGameDetailPage` | Proposto e alinhado na discussão | y |
| Confirmação DELETE | `AlertDialog` shadcn (não `window.confirm`) | Estudo + UX; mesmo fluxo confirm→ação | y |
| Status / nota | `Select` shadcn | Substitui `<select>` nativo | y |
| Painéis Progresso / Sessões | `Card` (+ header/content) | Alinhamento pedido no UAT | y |
| Tema | Dark Letterboxd tokens (bg ~`#12161a`, accent verde) via CSS variables do shadcn | AD-012 | y |
| Fontes | Manter Fraunces + DM Sans (index.html) mapeadas no tema Tailwind se possível | Visual já aprovado | y |
| Nav / lista / busca | Permanecem no CSS global atual | Fora do slice | y |
| Testes | Atualizar TL da detail para Select/AlertDialog; mensagens PT e calls API iguais | Evidence-or-zero do ui-v1 | y |
| Auth / rate limit / pagamentos / offline | N/A | Single-user local | n |
| remaining dimensions | N/A for this scope | Feature é só UI shell; sem persistência nova | n |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Scaffold Tailwind + shadcn ⭐ MVP

**User Story**: As a developer learning UI, I want Tailwind and shadcn set up in `frontend/` so that I can add owned components without starting from scratch.

**Why P1**: Sem scaffold não há componentes.

**Acceptance Criteria**:

1. The system SHALL include Tailwind CSS configured for the Vite React TypeScript app under `frontend/`.
2. The system SHALL include a shadcn `components.json` and base utilities (`cn`, theme CSS variables) under `frontend/`.
3. WHEN `npm run build` runs in `frontend/` THEN the build SHALL succeed with Tailwind + shadcn base present.
4. The system SHALL keep dark-only theming as the default app appearance (no light scheme required).

**Independent Test**: Fresh `npm install && npm run build` in `frontend/` succeeds; theme CSS variables exist.

---

### P1: Detalhe com Select, Card e Input ⭐ MVP

**User Story**: As a player, I want status, rating, and session fields on the detail page to use consistent shadcn controls so that logging feels aligned and intentional.

**Why P1**: Motivo do estudo + melhora o painel estranho do UAT.

**Acceptance Criteria**:

1. WHEN the user opens `/games/{id}` for an existing game THEN the system SHALL show Progresso and Sessões inside Card-style panels.
2. WHEN the user changes status via the Status control THEN the system SHALL call `PATCH /tracked-games/{id}` with that `status` and refresh the displayed status (PT labels unchanged).
3. WHEN the user sets a rating from 1 to 10 via the Nota control THEN the system SHALL call `PATCH` with that `rating` and refresh the displayed rating.
4. WHEN the user submits a session with `durationMinutes` > 0 THEN the system SHALL call `POST .../sessions` and refresh sessions and total play time.
5. IF session create is submitted with `durationMinutes` ≤ 0 or non-numeric THEN the system SHALL NOT call the API and SHALL show the existing Portuguese validation message.
6. The system SHALL preserve Portuguese labels and accessible names needed by existing tests where behavior is unchanged (Status, Nota, Duração (min), Registrar sessão, etc.).

**Independent Test**: Open a tracked game; change status and rating; add a 90‑min session; invalid duration shows PT error without API call.

---

### P1: Confirmações com AlertDialog ⭐ MVP

**User Story**: As a player, I want delete confirmations in-app so that I do not rely on the browser confirm dialog.

**Why P1**: Parte do estudo shadcn + melhora UX.

**Acceptance Criteria**:

1. WHEN the user activates Remover on a session THEN the system SHALL show an AlertDialog asking for confirmation before calling DELETE.
2. WHEN the user confirms session deletion THEN the system SHALL call `DELETE .../sessions/{sessionId}` and refresh sessions and total play time.
3. WHEN the user cancels the session delete dialog THEN the system SHALL NOT call DELETE.
4. WHEN the user activates Remover jogo THEN the system SHALL show an AlertDialog before calling DELETE on the tracked game.
5. WHEN the user confirms game deletion THEN the system SHALL call `DELETE /tracked-games/{id}` and navigate to `/`.
6. WHEN the user cancels game deletion THEN the system SHALL NOT call DELETE and SHALL remain on the detail page.

**Independent Test**: Cancel delete → no API; confirm session delete → list/total update; confirm game delete → land on `/`.

---

### P2: Tema Letterboxd nos tokens shadcn

**User Story**: As a player, I want the shadcn controls to match the dark diary theme so that the detail page does not look like a stock shadcn demo.

**Why P2**: Visual continuity with AD-012.

**Acceptance Criteria**:

1. The system SHALL map shadcn theme CSS variables so the detail page background and primary accent read as dark diary (near `#12161a` canvas, green primary akin to `#00c030`).
2. The system SHALL NOT require a light theme toggle in this feature.

**Independent Test**: Visual review of detail page: dark panels, green primary actions, no light flash as default.

---

## Edge Cases

- IF `GET /tracked-games/{id}` returns 404 THEN the system SHALL keep the existing PT not-found UI (shadcn not required on that path).
- IF a mutating request fails THEN the system SHALL show a PT error and keep previous successful data on screen.
- IF AlertDialog is open THEN Escape / cancel control SHALL dismiss without side effects.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| SHAD-01 | P1: Scaffold | Execute | ✅ Verified |
| SHAD-02 | P1: Scaffold | Execute | ✅ Verified |
| SHAD-03 | P1: Scaffold | Execute | ✅ Verified |
| SHAD-04 | P1: Scaffold | Execute | ✅ Verified |
| SHAD-05 | P1: Detalhe controls | Execute | ✅ Verified |
| SHAD-06 | P1: Detalhe controls | Execute | ✅ Verified |
| SHAD-07 | P1: Detalhe controls | Execute | ✅ Verified |
| SHAD-08 | P1: Detalhe controls | Execute | ✅ Verified |
| SHAD-09 | P1: Detalhe controls | Execute | ⚠️ Partial (≤0 tested; non-numeric gap) |
| SHAD-10 | P1: Detalhe controls | Execute | ✅ Verified |
| SHAD-11 | P1: AlertDialog | Execute | ✅ Verified |
| SHAD-12 | P1: AlertDialog | Execute | ✅ Verified |
| SHAD-13 | P1: AlertDialog | Execute | ✅ Verified |
| SHAD-14 | P1: AlertDialog | Execute | ✅ Verified |
| SHAD-15 | P1: AlertDialog | Execute | ✅ Verified |
| SHAD-16 | P1: AlertDialog | Execute | ✅ Verified |
| SHAD-17 | P2: Theme tokens | Execute | ⚠️ UAT |
| SHAD-18 | P2: Theme tokens | Execute | ✅ Verified |

**Coverage:** 18 total; Verifier PASS with minor gaps above.

---

## Success Criteria

- [ ] `frontend/` builds with Tailwind + shadcn
- [ ] Detail page uses Card / Select / Input / Button / AlertDialog for log flows
- [ ] Existing behavior ACs of ui-v1 detail still hold (API calls + PT messages)
- [ ] Dark Letterboxd look preserved on detail
- [ ] Bruno can inspect `components/ui/*` as owned study code
