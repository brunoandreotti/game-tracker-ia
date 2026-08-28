# Rating 0–5 + stars on list — Specification

## Problem Statement

A nota do diário usa escala 1–10, desalinhada do desejo de uma escala curta 0–5 e de um sinal visual de estrelas na lista. Sem mudar API + Select + listagem juntos, a UI e a validação ficam inconsistentes.

## Goals

- [ ] PATCH / listagem / detalhe usam a mesma escala **0–5** (ou `null` = sem nota)
- [ ] Lista `/` mostra nota em estrelas; detalhe mantém Select numérico
- [ ] Notas históricas > 5 são clampadas para 5 na migration

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Estrelas clicáveis / input por estrela no detalhe | Bruno: Select numérico no detalhe |
| Meias estrelas (0.5) | Escala inteira 0–5 |
| Limpar nota via `PATCH` com `rating: null` | Mantém regra library-v1 (null no body = no-op) |
| Migração de lista/busca para shadcn | Outra feature; só mudar o display da nota na lista |
| Auth / multi-user | Fora do produto v1 |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --------------------- | -------------- | --------- | ---------- |
| Escala + Sem nota | Inteiro **0–5** + `null` = Sem nota | Recomendação C; Bruno: “faça o que vc recomenda” | y |
| UI detalhe vs lista | Select numérico no detalhe; estrelas **só** na listagem `/` | Confirmado por Bruno | y |
| Migração 6–10 | Clamp para **5** | Preserva “teve nota alta” sem inventar remapeamento | y |
| Selecionar “Sem nota” no Select | Não chama API (no-op) | Igual comportamento atual; limpar nota fora do v1 | y |
| `0` vs `null` na lista | `null` → “Sem nota”; `0` → 0 de 5 estrelas preenchidas | Distingue “ainda não avaliou” de “avaliou zero” | y |
| Constraint SQL | Flyway UPDATE clamp; CHECK `(rating IS NULL OR rating BETWEEN 0 AND 5)` se couber limpo | Evita dados fora da faixa após deploy | y (agent) |

**Open questions:** none — all resolved or logged above.

---

## User Stories

### P1: Escala de nota 0–5 na API ⭐ MVP

**User Story**: As a player, I want ratings stored as 0–5 (or unset) so that the diary scale matches a short star-friendly range.

**Why P1**: Sem API correta, a UI não pode persistir 0–5.

**Acceptance Criteria** (each line is one EARS pattern):

1. WHEN the client sends `PATCH /tracked-games/{id}` with `rating` equal to an integer from 0 through 5 inclusive THEN the system SHALL return HTTP 200 and persist that `rating`.
2. IF `rating` is present in the PATCH body and is not an integer in 0–5 THEN the system SHALL return HTTP 400 and a JSON body with `status`, `error`, and `message`.
3. WHEN `PATCH /tracked-games/{id}` includes `rating` as `null` THEN the system SHALL leave the stored rating unchanged (no clear).
4. WHEN the database migration for this feature runs THEN the system SHALL set any stored `rating` greater than 5 to 5 and any stored `rating` less than 0 to 0.
5. The system SHALL allow `rating` to remain `null` on create and on read when unset.

**Independent Test**: PATCH rating 0 and 5 succeed; PATCH 6 and −1 return 400; after migration a row that had 9 reads as 5; PATCH with `"rating": null` leaves prior value.

---

### P1: Select numérico 0–5 no detalhe ⭐ MVP

**User Story**: As a player, I want a numeric Nota select from 0 to 5 on the detail page so that I can set ratings without learning a new control.

**Why P1**: Continuidade do fluxo de log no detalhe (shadcn Select).

**Acceptance Criteria**:

1. WHEN the user opens `/games/{id}` THEN the system SHALL show a Nota `Select` whose options are Sem nota plus the integers 0, 1, 2, 3, 4, and 5.
2. WHEN the user selects an integer rating from 0 through 5 THEN the system SHALL call `PATCH /tracked-games/{id}` with that `rating` and refresh the displayed rating text.
3. WHEN the displayed rating is `null` THEN the system SHALL show the Select value as Sem nota and SHALL show “Sem nota” in the detail summary (not “Nota 0”).
4. WHEN the user selects Sem nota THEN the system SHALL NOT call `PATCH` solely to clear the rating.
5. IF the client-side rating value is outside 0–5 THEN the system SHALL show a Portuguese validation message that the rating must be between 0 and 5 and SHALL NOT call the API.

**Independent Test**: Set rating 0 and 5 on detail; summary shows Nota 0 / Nota 5; Sem nota option does not fire PATCH; invalid client path shows PT message.

---

### P1: Estrelas na listagem ⭐ MVP

**User Story**: As a player, I want to see ratings as stars on the home list so that scores are glanceable without opening detail.

**Why P1**: Pedido explícito; lista é o único lugar com estrelas neste slice.

**Acceptance Criteria**:

1. WHEN the user opens `/` and a tracked game has `rating` equal to an integer N from 0 through 5 THEN the system SHALL display that rating as a star indicator with N filled stars out of 5 (and SHALL NOT show the text form `Nota N` for that rating).
2. WHEN a tracked game on `/` has `rating` null THEN the system SHALL show “Sem nota” and SHALL NOT show filled stars for that entry.
3. The detail page `/games/{id}` SHALL NOT use star glyphs as the primary rating display (text + numeric Select only).

**Independent Test**: List shows stars for rated games and “Sem nota” for null; detail still shows Nota N / Sem nota and numeric Select.

---

## Edge Cases

- IF PATCH sends `rating` 6 (or any value outside 0–5) THEN the system SHALL return HTTP 400.
- WHEN `rating` is 0 on the list THEN the system SHALL show zero filled stars (not the label “Sem nota”).
- WHEN `rating` is 0 on the detail THEN the system SHALL show “Nota 0” and Select value `0`.
- IF the API returns 400 on rating PATCH THEN the detail page SHALL show the error in Portuguese without clearing the previous successful rating display until a successful refresh.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| RATE-01 | P1: Escala de nota 0–5 na API | Execute | ✅ Verified |
| RATE-02 | P1: Escala de nota 0–5 na API | Execute | ✅ Verified |
| RATE-03 | P1: Escala de nota 0–5 na API | Execute | ✅ Verified |
| RATE-04 | P1: Escala de nota 0–5 na API | Execute | ✅ Verified |
| RATE-05 | P1: Escala de nota 0–5 na API | Execute | ✅ Verified |
| RATE-06 | P1: Select numérico 0–5 no detalhe | Execute | ✅ Verified |
| RATE-07 | P1: Select numérico 0–5 no detalhe | Execute | ✅ Verified |
| RATE-08 | P1: Select numérico 0–5 no detalhe | Execute | ✅ Verified |
| RATE-09 | P1: Select numérico 0–5 no detalhe | Execute | ✅ Verified |
| RATE-10 | P1: Select numérico 0–5 no detalhe | Execute | ✅ Verified |
| RATE-11 | P1: Estrelas na listagem | Design | Pending |
| RATE-12 | P1: Estrelas na listagem | Design | Pending |
| RATE-13 | P1: Estrelas na listagem | Design | Pending |

**ID format:** `RATE-NN`

**Coverage:** 13 total, 0 mapped to tasks, 13 unmapped ⚠️ (map after Tasks)

**Supersedes (bounds only):** library-v1 rating 1–10 ACs (e.g. LIB-17), ui-v1 UI-15 rating 1–10, ui-shadcn SHAD rating 1–10 wording — product docs updated to 0–5.

---

## Success Criteria

- [ ] Demo: acompanhar jogo → PATCH nota 5 → lista mostra 5 estrelas → detalhe Select em 5
- [ ] Demo: PATCH nota 0 → lista mostra 0 preenchidas + “não é Sem nota”; detalhe “Nota 0”
- [ ] `mvn test` e testes Vitest da lista/detalhe verdes com a nova escala
- [ ] Migration clamp: valor 9 vira 5
