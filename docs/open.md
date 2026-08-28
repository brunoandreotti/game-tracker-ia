# Ainda em aberto

API library-v1 e UI v1 (happy path + dark AD-012) em código. Próximo estudo de UI: **shadcn + Tailwind** (AD-013). Brief em [product.md](product.md) e [stack.md](stack.md).

## Na implementação (não precisa cravar agora)

- Versões finas Tailwind/shadcn no dia do setup (Context7).
- Quão longe migrar CSS global vs deixar coexistir na 1ª leva.
- Detalhe de packing CORS se houver mais de uma origem além de `5173`.

## Processo

- UI v1: implementada + validation PASS; polish dark feito.
- shadcn (AD-013): decidido; **código só com permissão explícita**.
- Não mover backend para `api/` neste passo.
