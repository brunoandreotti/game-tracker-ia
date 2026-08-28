# Rating 0–5 + stars Validation

**Date**: 2026-08-28
**Spec**: `.specs/features/rating-0-5/spec.md`
**Diff range**: `cb447fc^..HEAD` (docs + T1–T5 + gap fixes; latest `aa9ee25`)
**Verifier**: independent re-verify (iteration 2/3; prior FAIL gaps addressed)

---

## Task Completion

| Task | Status  | Notes |
| ---- | ------- | ----- |
| T1   | ✅ Done | Flyway V2 clamp + CHECK |
| T2   | ✅ Done | `@Min(0) @Max(5)` + Spock |
| T3   | ✅ Done | Detail Select 0–5 + Vitest |
| T4   | ✅ Done | List stars + Vitest |
| T5   | ✅ Done | Bruno sample `rating: 5` |

---

## Spec-Anchored Acceptance Criteria

| ID | Criterion | Spec-defined outcome | `file:line` + assertion | Result |
| -- | --------- | -------------------- | ----------------------- | ------ |
| RATE-01 | PATCH rating 0–5 → persist | HTTP 200; body rating equals sent | `TrackedGameControllerSpec.groovy:179-180` - `status().isOk()`; `jsonPath('$.rating').value(5)` · `:199-200` - `.value(0)` | ✅ PASS |
| RATE-02 | rating outside 0–5 → 400 | HTTP 400; JSON `status`, `error`, `message` | `TrackedGameControllerSpec.groovy:238-241` - `isBadRequest()`; `$.status` 400; `$.error` "Bad Request"; `$.message`.exists() | ✅ PASS |
| RATE-03 | PATCH `rating: null` → no clear | Stored rating unchanged | `TrackedGameServiceImplSpec.groovy` - patch with null rating leaves prior value | ✅ PASS |
| RATE-04 | Migration clamp >5→5, <0→0 | Rows clamped; CHECK enforced | `JpaTrackedGameRepositorySpec.groovy:77` - clamp UPDATE yields 5 · `:96` / `:106` - CHECK rejects 9 and −1 | ✅ PASS |
| RATE-05 | null on create/read when unset | rating null | `TrackedGameControllerSpec.groovy:51` - `jsonPath('$.rating').isEmpty()` | ✅ PASS |
| RATE-06 | Select Sem nota + 0–5 only | Seven options; no 6–10 | `TrackedGameDetailPage.test.tsx:141-146` - options present; `queryByRole('option', { name: '6'..'10' })` absent | ✅ PASS |
| RATE-07 | Select 0–5 → PATCH + refresh | API called; summary updated | `TrackedGameDetailPage.test.tsx` - `toHaveBeenCalledWith(1, { rating: 5 })` / `{ rating: 0 }` | ✅ PASS |
| RATE-08 | null → Sem nota in summary | Text Sem nota (not Nota 0) | `TrackedGameDetailPage.test.tsx:70` - `getByText(/… Sem nota …/)` | ✅ PASS |
| RATE-09 | Sem nota → no PATCH | API not called | `TrackedGameDetailPage.test.tsx:190` area - `patchTrackedGame).not.toHaveBeenCalled()` | ✅ PASS |
| RATE-10 | Client outside 0–5 → PT message, no API | Message mentions 0 and 5 | `ratingBounds.test.ts:19-33` - `isValidRating(6)` false; message contains `entre 0 e 5` | ✅ PASS |
| RATE-11 | List rated → N/5 stars, not `Nota N` | Stars + no Nota N text | `TrackedGamesPage.test.tsx:87-89` - `getByLabelText('Nota 5 de 5')` ★★★★★; `queryByText(/Nota 5/)` absent | ✅ PASS |
| RATE-12 | List null → Sem nota | Sem nota; no filled stars | `TrackedGamesPage.test.tsx:86` - `getByText(/Sem nota/)` | ✅ PASS |
| RATE-13 | Detail no stars as primary | Text + Select only | `TrackedGameDetailPage.test.tsx:160-161` - no `.rating-stars`; no `Nota \d+ de 5` label | ✅ PASS |

**Status**: ✅ All 13 ACs evidenced

---

## Edge Cases

| Edge case | Evidence | Result |
| --------- | -------- | ------ |
| PATCH rating 6 → 400 | `TrackedGameControllerSpec.groovy:238-241` | ✅ PASS |
| List rating 0 → ☆☆☆☆☆ not Sem nota | `TrackedGamesPage.test.tsx:88` | ✅ PASS |
| Detail rating 0 → Nota 0 | `TrackedGameDetailPage.test.tsx` rating 0 case | ✅ PASS |
| API 400 on rating PATCH keeps prior | `TrackedGameDetailPage.test.tsx:371-372` - alert + still Nota 3 | ✅ PASS |

---

## Discrimination Sensor

Scratch: git worktree at `aa9ee25` under `%LOCALAPPDATA%\Temp\rating-0-5-sensor`; removed after run. Real tree porcelain unchanged (lessons/validation docs only).

| Mutation | Description | Killed? |
| -------- | ----------- | ------- |
| 1 | `@Max(5)` → `@Max(10)` | ✅ Killed — `TrackedGameControllerSpec` rating-6 → 400 failed |
| 2 | List `<RatingStars>` → `` `Nota ${rating}` `` | ✅ Killed — `TrackedGamesPage.test.tsx:87` |
| 3 | `RATING_VALUES` extended to 0–10 | ✅ Killed — detail options test (option `6` present) |

**Sensor depth**: lightweight (3 mutations)
**Result**: 3/3 killed — ✅ PASS

---

## Gate Check

| Gate | Command | Result |
| ---- | ------- | ------ |
| Feature Spock | `mvn test -Dtest=TrackedGameControllerSpec,JpaTrackedGameRepositorySpec` | ✅ passed (retry after unrelated WireMock flake on SessionControllerSpec) |
| Frontend | `cd frontend && npm test -- --run` | ✅ **54** passed |

---

## Summary

**Overall**: ✅ PASS — Ready

**Spec-anchored**: 13/13 ACs
**Sensor**: 3/3 killed
**Prior gaps** (RATE-02/04/06/10/13 + edge + sensor option mutant): closed in `aa9ee25`

**Next**: Interactive UAT — lista com estrelas; detalhe Select 0–5; restart API para Flyway V2 se o Postgres local ainda estiver na V1.
