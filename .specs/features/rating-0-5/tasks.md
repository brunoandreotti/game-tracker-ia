# Rating 0–5 + stars — Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.**

**Design**: skipped (no new architecture; bounds + UI display only)
**Spec**: `.specs/features/rating-0-5/spec.md`
**Status**: Approved (Bruno: continue TLC flow)

---

## Test Coverage Matrix

> Guidelines: `AGENTS.md` (Spock Given/When/Then; Vitest for UI). Strong defaults for AC coverage.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| ---------- | ------------------ | -------------------- | ---------------- | ----------- |
| Flyway migration | none | Build/integration: migration applies on Testcontainers | `src/main/resources/db/migration/` | `mvn test` (via existing Postgres specs) |
| PATCH validation DTO | component (Spock) | RATE-01/02/03; edge rating 6 → 400; 0 and 5 → 200 | `*TrackedGameControllerSpec.groovy` | `mvn test -Dtest=TrackedGameControllerSpec` |
| Service fixtures / demo | unit + component | Fixtures ≤5; demo PATCH rating 5 | `*TrackedGameServiceImplSpec.groovy`, `*SessionControllerSpec.groovy` | `mvn test -Dtest=TrackedGameServiceImplSpec,SessionControllerSpec` |
| Detail page Select | unit (Vitest/TL) | RATE-06–10; rating 0/5; Sem nota no PATCH | `frontend/src/pages/TrackedGameDetailPage.test.tsx` | `cd frontend && npm test` |
| List stars | unit (Vitest/TL) | RATE-11/12; null → Sem nota; 0 → zero filled | `frontend/src/pages/TrackedGamesPage.test.tsx` (+ helper test if split) | `cd frontend && npm test` |

## Gate Check Commands

| Gate Level | When to Use | Command |
| ---------- | ----------- | ------- |
| Quick | Frontend-only tasks | `cd frontend && npm test` |
| Full | Backend API/spec tasks | `mvn test -Dtest=TrackedGameControllerSpec,TrackedGameServiceImplSpec,SessionControllerSpec,JpaTrackedGameRepositorySpec` |
| Build | Phase end / migration | `mvn -q test` then `cd frontend && npm test` (after UI tasks) |

---

## Execution Plan

### Phase 1: API + DB

```
T1 → T2
```

### Phase 2: UI

```
T2 → T3 → T4
```

### Phase 3: Contract samples

```
T2 → T5
```

---

## Task Breakdown

### T1: Flyway clamp rating to 0–5

**What**: Add migration that clamps `rating` > 5 to 5, < 0 to 0, and adds CHECK `(rating IS NULL OR (rating >= 0 AND rating <= 5))`.
**Where**: `src/main/resources/db/migration/V2__rating_0_to_5.sql`
**Depends on**: None
**Reuses**: V1 `tracked_game.rating`
**Requirement**: RATE-04

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Migration file present and ordered after V1
- [x] UPDATE clamp + CHECK as specified
- [x] Gate: existing Postgres specs still start (Flyway applies)

**Tests**: none
**Gate**: full
**Status**: ✅ Complete

**Commit**: `feat(api): clamp tracked game rating to 0-5`

---

### T2: PATCH validation 0–5 + Spock updates

**What**: Change `@Min`/`@Max` on PATCH rating to 0–5; update controller/service/session Spock specs to use in-range values and reject 6 (not 11).
**Where**: `src/main/java/.../PatchTrackedGameRequest.java` (+ Spock specs under `src/test/groovy/...`)
**Depends on**: T1
**Reuses**: Existing Bean Validation + controller specs
**Requirement**: RATE-01, RATE-02, RATE-03, RATE-05

**Tools**:

- MCP: `user-context7` (Jakarta Validation Min/Max if needed)
- Skill: `tlc-spec-driven`

**Done when**:

- [x] `@Min(0) @Max(5)` on `PatchTrackedGameRequest.rating`
- [x] PATCH 0 and 5 → 200; PATCH 6 → 400
- [x] Service fixtures and demo flow use ≤5 (e.g. 5 instead of 9)
- [x] `rating: null` PATCH no-op still covered by existing behavior
- [x] Gate full passes

**Tests**: component (Spock)
**Gate**: full
**Status**: ✅ Complete

**Commit**: `feat(api)!: accept rating 0-5 on tracked game PATCH`

---

### T3: Detail Select options 0–5

**What**: Update detail Nota Select to Sem nota + 0–5; client validation message 0–5; refresh tests (rating 5 instead of 9).
**Where**: `frontend/src/pages/TrackedGameDetailPage.tsx`
**Depends on**: T2
**Reuses**: shadcn Select pattern
**Requirement**: RATE-06, RATE-07, RATE-08, RATE-09, RATE-10

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [x] Select options: Sem nota, 0, 1, 2, 3, 4, 5
- [x] Selecting 0–5 PATCHes; Sem nota does not PATCH
- [x] Summary shows Nota 0 / Sem nota correctly
- [x] PT validation mentions 0 and 5
- [x] Vitest gate green

**Tests**: unit
**Gate**: quick
**Status**: ✅ Complete

**Commit**: `feat(ui): use 0-5 numeric rating select on detail`

---

### T4: Star rating on list

**What**: Replace `Nota N` on `/` with N-of-5 star indicator; null stays “Sem nota”; keep detail without stars as primary display.
**Where**: `frontend/src/pages/TrackedGamesPage.tsx`
**Depends on**: T3
**Reuses**: list meta line layout
**Requirement**: RATE-11, RATE-12, RATE-13

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Rated games show filled/empty stars (N/5), not `Nota N`
- [ ] null → Sem nota (no filled stars)
- [ ] rating 0 → zero filled (not Sem nota)
- [ ] Vitest covers list cases

**Tests**: unit
**Gate**: quick

**Commit**: `feat(ui): show rating as stars on tracked games list`

---

### T5: Update Bruno PATCH sample

**What**: Change Bruno patch example rating from 9 to 5.
**Where**: `bruno/tracked-games/patch.bru`
**Depends on**: T2
**Reuses**: existing Bruno request
**Requirement**: RATE-01 (sample contract)

**Tools**:

- MCP: NONE
- Skill: `tlc-spec-driven`

**Done when**:

- [ ] Body uses `"rating": 5`

**Tests**: none
**Gate**: build (`mvn -q -DskipTests compile` smoke OK; no UI)

**Commit**: `chore(bruno): use rating 5 in patch sample`

---

## Phase Execution Map

```
Phase 1 → Phase 2 → Phase 3

Phase 1:  T1 → T2
Phase 2:  T2 → T3 → T4
Phase 3:  T2 → T5
```

Preferred inline order: T1, T2, T3, T4, T5.

**Batches**: 5 tasks → single batch, execute inline (no sub-agents).

---

## Task Granularity Check

| Task | Scope | Status |
| ---- | ----- | ------ |
| T1 | 1 migration | OK |
| T2 | DTO + matching Spock | OK (cohesive) |
| T3 | Detail page + tests | OK |
| T4 | List page + stars | OK |
| T5 | 1 Bruno file | OK |

## Diagram-Definition Cross-Check

| Task | Depends On (body) | Diagram Shows | Status |
| ---- | ----------------- | ------------- | ------ |
| T1 | None | (start) | Match |
| T2 | T1 | T1 → T2 | Match |
| T3 | T2 | T2 → T3 (via phase order) | Match |
| T4 | T3 | T3 → T4 | Match |
| T5 | T2 | T2 → T5 | Match |

## Test Co-location Validation

| Task | Layer | Matrix Requires | Task Says | Status |
| ---- | ----- | --------------- | --------- | ------ |
| T1 | Flyway | none | none | OK |
| T2 | PATCH DTO + Spock | component | component | OK |
| T3 | Detail page | unit | unit | OK |
| T4 | List page | unit | unit | OK |
| T5 | Bruno sample | none | none | OK |
