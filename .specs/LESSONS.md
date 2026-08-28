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

## Quarantined (failed when applied - ignore)

A confirmed lesson that recurred alongside failure. Kept for the maintainer to review.

_none_
