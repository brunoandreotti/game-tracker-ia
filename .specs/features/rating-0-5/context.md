# Rating 0–5 + stars on list — Context

**Gathered:** 2026-08-28
**Spec:** `.specs/features/rating-0-5/spec.md`
**Status:** Ready for design / tasks (pending Bruno confirm of spec)

---

## Feature Boundary

Mudar a escala de nota de **1–10** para **0–5** (com `null` = sem nota) em API + UI. No detalhe, Select **numérico**; na listagem `/`, mostrar a nota em **estrelas**. Sem input por estrelas no detalhe. Sem “desavaliar” via `rating: null` (regra library-v1 mantida).

---

## Implementation Decisions

### Escala (recomendação C — confirmada)

- Valores válidos no PATCH: inteiro **0–5**
- `null` = ainda sem nota (default no POST)
- Select no detalhe: **Sem nota** (estado `null`) + **0, 1, 2, 3, 4, 5**
- Escolher “Sem nota” no Select **não** chama PATCH (no-op) — limpar nota continua fora do v1

### Listagem

- `rating === null` → texto **Sem nota** (sem estrelas preenchidas)
- `rating` 0–5 → **N estrelas preenchidas** de 5 (0 = só contorno / vazias; 5 = todas preenchidas)
- Detalhe continua texto `Nota N` / `Sem nota` + Select numérico (sem estrelas)

### Migração

- Notas existentes **> 5** → **clamp para 5** (Flyway)
- Notas **< 0** (se houver) → clamp para **0** (defensivo)

### Agent's Discretion

- Visual das estrelas (Unicode vs SVG vs CSS) desde que acessível e legível no tema dark
- Constraint SQL `CHECK` opcional se encaixar limpo na migration

### Declined / Undiscussed → Assumptions

- Meias estrelas / escala 0.5 → fora
- Estrelas clicáveis no detalhe → fora
- Limpar nota via API (`rating: null` grava null) → fora (library-v1)

---

## Specific References

- Discussão chat 2026-08-28: Bruno confirmou “faça o que vc recomenda” → C + clamp; detalhe Select numérico; estrelas só na lista
- `docs/product.md` (nota), `PatchTrackedGameRequest`, `TrackedGamesPage`, `TrackedGameDetailPage`
- Supersede bounds de library-v1 LIB-17 e UI-15 / SHAD ACs de 1–10

---

## Deferred Ideas

- Limpar / desavaliar nota
- Input por estrelas no detalhe
- Meias estrelas estilo Letterboxd
