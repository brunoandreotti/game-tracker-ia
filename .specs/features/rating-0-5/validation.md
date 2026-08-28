# Rating 0–5 + stars Validation

**Date**: 2026-08-28
**Spec**: `.specs/features/rating-0-5/spec.md`
**Diff range**: `cb447fc^..HEAD` (includes fix `aa9ee25`)
**Verifier**: independent sub-agent (author ≠ verifier, iteration 2)

---

## Task Completion

| Task | Status  | Notes |
| ---- | ------- | ----- |
| T1   | ✅ Done | Flyway V2 migration present |
| T2   | ✅ Done | `@Min(0) @Max(5)` + Spock updates |
| T3   | ✅ Done | Detail Select 0–5 + Vitest |
| T4   | ✅ Done | List stars + Vitest |
| T5   | ✅ Done | Bruno sample `rating: 5` |

All T1–T5 marked ✅ Complete in `tasks.md`.

---

## Spec-Anchored Acceptance Criteria

| ID | Criterion (WHEN X THEN Y) | Spec-defined outcome | `file:line` + assertion | Result |
| -- | ------------------------- | -------------------- | ----------------------- | ------ |
| RATE-01 | PATCH `rating` 0–5 → persist | HTTP 200; body `rating` equals sent value | `TrackedGameControllerSpec.groovy:179` - `status().isOk()`; `jsonPath('$.rating').value(5)` · `TrackedGameControllerSpec.groovy:199` - `jsonPath('$.rating').value(0)` | ✅ PASS |
| RATE-02 | `rating` outside 0–5 → 400 | HTTP 400; JSON with `status`, `error`, **and** `message` | `TrackedGameControllerSpec.groovy:238` - `status().isBadRequest()`; `jsonPath('$.status').value(400)` · `:240` - `jsonPath('$.error').value("Bad Request")` · `:241` - `jsonPath('$.message').exists()` | ✅ PASS |
| RATE-03 | PATCH `rating: null` → no clear | Stored rating unchanged | `TrackedGameServiceImplSpec.groovy:138` - `result.rating() == 4` after `service.patch(1L, PlayStatus.COMPLETED, null)` | ✅ PASS |
| RATE-04 | Migration runs → clamp >5 to 5, <0 to 0 | DB rows clamped; CHECK enforced | `JpaTrackedGameRepositorySpec.groovy:77` - `queryForObject(..., Integer) == 5` after clamp UPDATE · `:96` - `thrown(DataIntegrityViolationException)` for rating 9 insert · `:106` - `thrown(DataIntegrityViolationException)` for rating −1 insert | ✅ PASS |
| RATE-05 | `rating` null on create/read when unset | `rating` null in response | `TrackedGameControllerSpec.groovy:51` - `jsonPath('$.rating').isEmpty()` · `TrackedGameServiceImplSpec.groovy:41` - `result.rating() == null` | ✅ PASS |
| RATE-06 | Detail Select options Sem nota + 0–5 | Seven options only | `TrackedGameDetailPage.test.tsx:141-147` - `findByRole('option', { name: optionName })` for Sem nota + 0–5 · `queryByRole('option', { name: invalidOption }).not.toBeInTheDocument()` for 6–10 | ✅ PASS |
| RATE-07 | Select 0–5 → PATCH + refresh | `patchTrackedGame` called; summary updated | `TrackedGameDetailPage.test.tsx:183` - `toHaveBeenCalledWith(1, { rating: 5 })` · `:207` - `{ rating: 0 }` | ✅ PASS |
| RATE-08 | `rating` null → Sem nota in summary | Summary text "Sem nota" (not "Nota 0") | `TrackedGameDetailPage.test.tsx:70` - `getByText(/… Sem nota …/)` | ✅ PASS |
| RATE-09 | Select Sem nota → no PATCH | API not called | `TrackedGameDetailPage.test.tsx:226` - `patchTrackedGame).not.toHaveBeenCalled()` | ✅ PASS |
| RATE-10 | Client value outside 0–5 → PT message, no API | Message mentions 0 and 5; no PATCH | `ratingBounds.test.ts:18-23` - `isValidRating(6/−1/10).toBe(false)` · `:31-33` - `message).toContain('A nota deve ser um número entre 0 e 5.')` | ⚠️ Spec-precision gap — no page-level assertion that `patchTrackedGame` is not called on invalid input (early return at `TrackedGameDetailPage.tsx:147-149`; Select prevents invalid selection) |
| RATE-11 | List rated game → N filled stars, not `Nota N` | Stars + no `Nota N` text | `TrackedGamesPage.test.tsx:87` - `getByLabelText('Nota 5 de 5')).toHaveTextContent('★★★★★')` · `:89` - `queryByText(/Nota 5/).not.toBeInTheDocument()` | ✅ PASS |
| RATE-12 | List `rating` null → "Sem nota", no filled stars | "Sem nota" label; no ★ for null entry | `TrackedGamesPage.test.tsx:86` - `getByText(/Sem nota/)` (null game; no star aria-label for Game One) | ✅ PASS |
| RATE-13 | Detail page no star glyphs as primary display | Text + numeric Select only | `TrackedGameDetailPage.test.tsx:160-161` - `querySelector('.rating-stars')).not.toBeInTheDocument()` · `queryByLabelText(/Nota \d+ de 5/).not.toBeInTheDocument()` | ✅ PASS |

**Status**: ✅ All ACs covered — 1 spec-precision gap (RATE-10 no-API clause)

---

## Edge Cases

| Edge case | Spec-defined outcome | Evidence | Result |
| --------- | -------------------- | -------- | ------ |
| PATCH `rating` 6 → 400 | HTTP 400 | `TrackedGameControllerSpec.groovy:238` - `status().isBadRequest()` (+ error/message at :240-241) | ✅ PASS |
| List `rating` 0 → zero filled stars | ☆☆☆☆☆, not "Sem nota" | `TrackedGamesPage.test.tsx:88` - `getByLabelText('Nota 0 de 5')).toHaveTextContent('☆☆☆☆☆')` | ✅ PASS |
| Detail `rating` 0 → "Nota 0" + Select `0` | Summary + PATCH | `TrackedGameDetailPage.test.tsx:207-208` | ✅ PASS |
| API 400 on rating PATCH → PT error, keep prior display | Error in PT; prior rating visible | `TrackedGameDetailPage.test.tsx:371-372` - `getByRole('alert')).toHaveTextContent('A nota deve estar entre 0 e 5.')` · `getByText(/Nota 3/)` retained | ✅ PASS |

---

## Discrimination Sensor

Scratch: temp git worktree at `aa9ee25` with `npm ci` in scratch `frontend/`; removed after run. Real-tree porcelain unchanged aside from this report rewrite.

| Mutation | File:line | Description | Killed? |
| -------- | --------- | ----------- | ------- |
| 1 | `PatchTrackedGameRequest.java:8` | `@Max(5)` → `@Max(10)` | ✅ Killed — `TrackedGameControllerSpec` (rating 6 → 400) failed |
| 2 | `TrackedGamesPage.tsx:28` | `return <RatingStars …>` → `` `Nota ${rating}` `` | ✅ Killed — `TrackedGamesPage.test.tsx:87` (`getByLabelText('Nota 5 de 5')`) failed |
| 3 | `ratingBounds.ts:5` | `RATING_VALUES` extended with 6–10 | ✅ Killed — `TrackedGameDetailPage.test.tsx:146` (`queryByRole('option', { name: '6' }).not.toBeInTheDocument()`) failed |

**Sensor depth**: lightweight (3 mutations)
**Result**: 3/3 killed — ✅ PASS

---

## Gate Check

| Gate | Command | Result |
| ---- | ------- | ------- |
| Full (feature scope) | `mvn test "-Dtest=TrackedGameControllerSpec,TrackedGameServiceImplSpec,SessionControllerSpec,JpaTrackedGameRepositorySpec"` | **41 passed**, 0 failed, 0 skipped |
| Build (backend) | `mvn -q test` | **70 passed**, 0 failed, 0 skipped |
| Build (frontend) | `cd frontend && npm test -- --run` | **55 passed**, 0 failed, 0 skipped |

**Test count delta (feature surface)**:
- `TrackedGameControllerSpec`: +1 (rating 0 PATCH); +2 assertions on rating-6 400 body (fix `aa9ee25`)
- `JpaTrackedGameRepositorySpec`: +3 (clamp UPDATE, CHECK rejects 9 and −1)
- `TrackedGameDetailPage.test.tsx`: +4 (option bounds, no-stars, rating PATCH 400 error)
- `ratingBounds.test.ts`: +4 (new file — scale, validation, message)
- No tests deleted without justification

**Skipped tests**: none

---

## Code Quality

| Principle | Status |
| --------- | ------ |
| Minimum code | ✅ Focused bounds + display changes |
| Surgical changes | ✅ Scoped to rating scale + stars |
| No scope creep | ✅ No auth, half-stars, or clear-via-null API |
| Matches patterns | ✅ Spock G/W/T; shadcn Select; Vitest TL |
| Spec-anchored outcome check | ✅ 12/13 full; 1 spec-precision gap (RATE-10 no-API) |
| Per-layer coverage expectation | ✅ Domain/API 1:1; UI unit tests for list + detail |
| Tests map to requirements | ✅ New tests align with rating stories |
| Guidelines | ✅ `AGENTS.md` Spock/Vitest conventions followed |

---

## Requirement Traceability Update

| Requirement | Previous Status | New Status |
| ----------- | --------------- | ---------- |
| RATE-01 | ❌ Needs Fix (iter 1) | ✅ Verified |
| RATE-02 | ❌ Needs Fix (iter 1) | ✅ Verified |
| RATE-03 | ✅ Verified | ✅ Verified |
| RATE-04 | ❌ Needs Fix (iter 1) | ✅ Verified |
| RATE-05 | ✅ Verified | ✅ Verified |
| RATE-06 | ❌ Needs Fix (iter 1) | ✅ Verified |
| RATE-07 | ✅ Verified | ✅ Verified |
| RATE-08 | ✅ Verified | ✅ Verified |
| RATE-09 | ✅ Verified | ✅ Verified |
| RATE-10 | ❌ Needs Fix (iter 1) | ⚠️ Verified (unit-layer; no-API clause spec-precision gap) |
| RATE-11 | ✅ Verified | ✅ Verified |
| RATE-12 | ✅ Verified | ✅ Verified |
| RATE-13 | ❌ Needs Fix (iter 1) | ✅ Verified |

---

## Summary

**Overall**: ✅ Ready

**Spec-anchored check**: 13/13 ACs evidenced; 1 spec-precision gap (RATE-10 no-API page assertion)
**Sensor**: 3/3 mutations killed
**Gate**: 70 backend + 55 frontend passed; feature Full gate 41/41 passed

**What works**: API accepts 0–5 and rejects 6 with full error body; migration clamp + CHECK tested; list stars and detail Select behave correctly including edge paths; fix `aa9ee25` closes all iteration-1 gaps except minor RATE-10 precision note.

**Issues found**: None blocking — RATE-10 "no API call" relies on unit-tested `isValidRating` + early return; optional follow-up Vitest if invalid input becomes injectable.

**Next steps**: Feature ready for demo / UAT; optional tighten RATE-10 with page-level no-PATCH assertion if desired.
