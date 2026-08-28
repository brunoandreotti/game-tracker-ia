# Ainda em aberto

API library-v1 e UI v1 + shadcn detalhe em código. Próxima feature planejada: **nota 0–5 + estrelas na lista** ([rating-0-5](../.specs/features/rating-0-5/spec.md), AD-014) — spec escrita; **código só com permissão explícita**.

## Na implementação (não precisa cravar agora)

- Visual exato das estrelas (Unicode vs SVG) — agent discretion na Execute.
- Constraint SQL CHECK na migration se couber limpo.

## Processo

- UI v1 / ui-shadcn: validation PASS.
- rating-0-5: Specify pronto (aguardando confirm); Design/Tasks/Execute em seguida.
- Não mover backend para `api/` neste passo.
