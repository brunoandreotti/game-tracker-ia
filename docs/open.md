# Ainda em aberto

API library-v1 e UI v1 + shadcn + rating-0-5 em código (Verifier PASS).

**Auth v1:** Specify + Design + Tasks prontos (T1–T35). Sessão pausada; Execute na **próxima sessão** — carregar Handoff em `.specs/STATE.md`, aprovar tasks se ainda Draft, depois pedido explícito de implementar. Preferir subagentes por batch (~35 tasks).

Guia de estudo do backend: **T35** → `docs/study/auth-v1-backend.md` (só no fim do Execute).

## Na implementação (não precisa cravar agora)

- Reiniciar a API local se o Postgres ainda não aplicou Flyway V2 (clamp + CHECK).
- UAT visual rating: estrelas na lista `/`, Select 0–5 no detalhe (ainda vale no diário atual, sem auth).

## Processo

- UI v1 / ui-shadcn / rating-0-5: validation PASS.
- Auth v1: artefatos em `.specs/features/auth-v1/`; ADs 015–017; sem código Java/React até Execute.
- Não mover backend para `api/` neste passo.
