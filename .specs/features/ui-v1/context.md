# UI v1 Context

**Gathered:** 2026-08-28
**Spec:** `.specs/features/ui-v1/spec.md`
**Status:** Tasks drafted — awaiting Bruno approval; Execute only with explicit code permission

---

## Feature Boundary

Primeira UI do Game Tracker: SPA local em `frontend/` que consome a API library-v1 (busca RAWG, jogos acompanhados, sessões). Happy path completo na interface; sem auth; sem stats; sem mover o backend para `api/`.

---

## Implementation Decisions

### Layout do repo (discutido, Guided)

- Criar só `frontend/` na raiz.
- Backend Spring permanece na raiz (`pom.xml`, `src/`, etc.).
- Mover para `api/` + `frontend/` fica para depois, se o Compose/orquestração pedir simetria.

### Escopo da 1ª UI (discutido, Guided)

- Busca → acompanhar → lista → detalhe (status + nota) → criar/apagar sessão → apagar jogo acompanhado.
- Não é “só lista + sessão” dependente de Bruno/curl para popular dados.

### Stack (discutido, Guided)

- React + Vite + TypeScript.
- Bruno conhece TypeScript “puro” e pouco de frameworks; o agente guia conceitos React sem jargão solto.
- Sem Next.js no v1.

### Navegação (discutido, Guided)

- `/` — lista de jogos acompanhados.
- `/search` — busca e ação de acompanhar.
- `/games/:id` — detalhe (`id` do jogo acompanhado, não `rawgId`).

### Tom visual (discutido, Guided)

- Diário pessoal minimal: tipografia clara, capas modestas, pouco chrome.
- Não catálogo/loja estilo Steam.

### Ritmo da discussão

- Guided (≤2 perguntas por turno). Assunções restantes registradas no spec.

### Agent's Discretion

- Escolha fina entre CSS Modules vs um único CSS global com variáveis (ambos permitidos; sem Tailwind).
- Biblioteca de roteamento (ex. React Router) e se usa `fetch` puro ou um client fino — Design.
- Formatação exata de `totalMinutes` (ex. `2h 30min`).
- Copy exata dos labels PT, desde que os enums da API não mudem.
- Como resolver “já acompanhado” (409) para deep-link ao detalhe se a lista precisar de um GET extra.

### Declined / Undiscussed Gray Areas → Assumptions

Registradas na tabela Assumptions do spec (default + razão), incluindo: UI em PT, CSS sem Tailwind, `VITE_API_URL`, CORS na Execute, checkbox `exact`, confirm em DELETE, empty/error/loading states, testes do front na Design.

---

## Specific References

- Product: diário pessoal, não rede social nem Steam ([docs/product.md](../../../docs/product.md)).
- Ordem “depois do v1”: “UI simples — lista + logar sessão” — expandido na discussão para happy path completo.
- Contrato HTTP inalterado: library-v1 / product.md.

---

## Deferred Ideas

- Monorepo `api/` + `frontend/`.
- Tailwind / design system.
- Filtro por status e stats na UI (quando a API tiver).
- PWA / mobile.
- Next.js.
