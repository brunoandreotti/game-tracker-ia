# LESSONS - auto-maintained by scripts/lessons.py

> Machine-owned. Do NOT hand-edit. Changes are overwritten on the next `lessons.py` write.
> Canonical state lives in `.specs/lessons.json`. Edit lessons only via the script.
> promote_threshold=2 distinct features · window_days=45 · quarantine_threshold=2

## Confirmed (load these at Specify/Design)

Corroborated across multiple features. Safe to apply as guidance.

_none_

## Candidates (under observation - do NOT load as guidance yet)

Seen once or not yet corroborated. Tracked, not trusted.

### L-001 - Page ACs need Testing Library assertions for happy path render, not only empty-state or client-side guards
- signal: `ac_gap` · recurrence: 1 feature(s) · scope: `frontend/pages` · harmful: 0
- features: ui-v1
- evidence: validation.md:P1 list content ACs (frontend/pages)
- last seen: 2026-08-28T18:40:37Z

### L-002 - Mutating UI flows must assert the API call payload and the refreshed on-screen state after success
- signal: `ac_gap` · recurrence: 1 feature(s) · scope: `frontend/pages` · harmful: 0
- features: ui-v1
- evidence: validation.md:P1 detail mutate ACs (frontend/pages)
- last seen: 2026-08-28T18:40:37Z

### L-003 - Visual style ACs that cannot be automated must be marked for interactive UAT instead of claimed as verified by build alone
- signal: `spec_precision_gap` · recurrence: 1 feature(s) · scope: `frontend` · harmful: 0
- features: ui-v1
- evidence: validation.md:P2 visual diary AC (frontend)
- last seen: 2026-08-28T18:40:37Z

### L-004 - Assert every branch named in an IF AC including non-numeric inputs when the criterion lists them
- signal: `spec_precision_gap` · recurrence: 1 feature(s) · scope: `frontend/pages` · harmful: 0
- features: ui-shadcn
- evidence: validation.md:SHAD-09 non-numeric duration (frontend/pages)
- last seen: 2026-08-28T20:17:54Z

### L-005 - Assert the Nota Select exposes exactly Sem nota plus integers 0-5, not just that individual options respond to clicks.
- signal: `surviving_mutant` · recurrence: 1 feature(s) · scope: `frontend/detail` · harmful: 0
- features: rating-0-5
- evidence: TrackedGameDetailPage.tsx:315 length:11 (frontend/detail)
- last seen: 2026-08-28T21:41:29Z

### L-006 - Add an integration test that inserts a legacy rating above 5 and asserts the Flyway clamp migration reads back 5.
- signal: `ac_gap` · recurrence: 1 feature(s) · scope: `db/migration` · harmful: 0
- features: rating-0-5
- evidence: RATE-04 (db/migration)
- last seen: 2026-08-28T21:41:29Z

### L-007 - For Bean Validation 400 responses, assert status, error, and message fields together, not status alone.
- signal: `ac_gap` · recurrence: 1 feature(s) · scope: `api/validation` · harmful: 0
- features: rating-0-5
- evidence: RATE-02 (api/validation)
- last seen: 2026-08-28T21:41:29Z

### L-008 - Cover client-side rating bounds with a Vitest that expects the Portuguese 0-5 message and no PATCH call.
- signal: `ac_gap` · recurrence: 1 feature(s) · scope: `frontend/detail` · harmful: 0
- features: rating-0-5
- evidence: RATE-10 (frontend/detail)
- last seen: 2026-08-28T21:41:30Z

## Quarantined (failed when applied - ignore)

A confirmed lesson that recurred alongside failure. Kept for the maintainer to review.

_none_
