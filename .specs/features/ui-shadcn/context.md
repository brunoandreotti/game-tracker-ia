# UI shadcn Context

**Gathered:** 2026-08-28
**Spec:** `.specs/features/ui-shadcn/spec.md`
**Status:** Ready for design

---

## Feature Boundary

Introduzir Tailwind + shadcn/ui no `frontend/` e migrar **apenas** a página de detalhe (`/games/:id`) para Card, Select, Input, Button e AlertDialog, preservando comportamento/API/PT do ui-v1 e o tema dark Letterboxd (AD-012). Lista e busca ficam fora.

---

## Implementation Decisions

### Stack

- shadcn/ui + Tailwind (não Mantine/antd/MUI) — AD-013
- Vite React TypeScript existente; sem Next.js

### Escopo de UI

- Slice 1: só `TrackedGameDetailPage`
- Nav, lista, busca: CSS global atual

### Interações

- Status e Nota: Select shadcn
- Sessão: Input (duração, data) + Button
- Painéis Progresso / Sessões: Card
- DELETE sessão e jogo: AlertDialog (substitui `window.confirm`)
- Mensagens PT de validação/erro: manter as mesmas strings do ui-v1

### Visual

- Dark-only; tokens shadcn mapeados para canvas escuro + accent verde
- Manter Fraunces + DM Sans já no `index.html`

### Agent's Discretion

- Nomes exatos dos arquivos gerados pelo CLI
- Estrutura interna dos wrappers (ex. helper de confirm) desde que ACs e testes passem
- Quanto do `global.css` permanece vs classes Tailwind na detail page

### Declined / Undiscussed Gray Areas → Assumptions

- Migrar lista/busca agora → fora (próxima feature)
- Toggle claro → não (AD-012)
- Remover `global.css` por completo → não nesta feature

---

## Specific References

- Letterboxd / Backloggd dark diary (AD-012)
- Discussão Mantine vs shadcn → shadcn para estudo (código no repo)

---

## Deferred Ideas

- Migrar `TrackedGamesPage` e `SearchPage` para shadcn
- Remover CSS legado não usado
- Extrair `LogPanel` compartilhado se lista/busca migrarem
