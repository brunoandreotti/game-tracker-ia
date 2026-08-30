# Auth v1 Context

**Gathered:** 2026-08-30
**Spec:** `.specs/features/auth-v1/spec.md`
**Status:** Design drafted, awaiting Bruno approval. Execute only with explicit code permission.

---

## Feature Boundary

Autenticação de estudo no Game Tracker: login **sem senha** (código de 6 dígitos por e-mail + Google), **mesma conta** quando o e-mail coincide, **diário isolado por usuário**. UI `/login` em dois passos, logout no header, rotas atuais exigem sessão. Sem senha, sem outros OAuth, sem papéis, sem migrar jogos já gravados.

---

## Implementation Decisions

### Ritmo da discussão

- Guided (≤2 perguntas por turno). Premissas restantes (sessão 7 dias, 429/60 s, 401 na API, etc.) registradas no spec.

### Dono do diário (discutido, Guided)

- Isolamento real: cada conta vê só os próprios jogos.
- API recusa quem não estiver logado (`401` em busca e `/tracked-games`).
- Unique de `rawgId` passa a ser por usuário, não global.

### Identidade (discutido, Guided)

- Google e OTP são dois caminhos para a mesma pessoa.
- Mesmo e-mail (normalizado) = a mesma conta e o mesmo diário.

### Cadastro e dados atuais (discutido, Guided)

- Sem tela de cadastro. O primeiro Google ou o primeiro OTP bem-sucedido cria o usuário.
- Jogos já existentes no Postgres **não** são atribuídos a ninguém. Conta nova nasce com lista vazia.

### Tela de login (discutido, Guided)

- Uma rota `/login` em dois passos: (1) Google ou e-mail + Enviar código; (2) campo de 6 dígitos na mesma rota.
- Sucesso → `/`. Logout no header. Português, shadcn, tema dark (AD-012 / AD-013).

### Código por e-mail (discutido, Guided)

- 6 dígitos, vale 10 minutos, no máximo 5 erros (depois o código some).
- Reenvio invalida o código anterior.
- No local, o e-mail cai numa caixa de dev (Mailpit), não SMTP de produção.

### Guia de estudo do backend (pedido 2026-08-30)

- Gerar **depois do Execute**, quando o código existir — um arquivo só, teoria + fluxo + classes deste repo.
- Caminho: [`docs/study/auth-v1-backend.md`](../../../docs/study/auth-v1-backend.md).
- Conteúdo: o que foi implementado e por quê; fluxo OTP e Google (sequência); sessão e isolamento; mapa do código hexagonal; trechos teóricos (OAuth 2 authorization code, OTP, por que não senha).
- Sem guia paralelo do frontend neste recorte (Bruno pediu a parte do backend).
- Não escrever o arquivo na Specify/Design: sem implementação ele ficaria genérico e desatualizaria.

### Agent's Discretion

- Cookie de sessão vs Bearer token (Design, com Context7 no Boot 4.1.1 / Security).
- Paths exatos do redirect Google (convenção Spring Security vs aliases em `/auth/...`).
- Como hashear/guardar o OTP (não ir em claro).
- Copy exata das mensagens em PT, desde que os status HTTP do spec não mudem.
- Nome da tabela/entidade de usuário, desde que o e-mail seja a chave de identidade.

### Declined / Undiscussed Gray Areas → Assumptions

Registradas na tabela Assumptions do spec (default + razão), incluindo: sessão 7 dias, rate limit 60 s / 429, superfície `401`, redirect da UI, falha Google e falha de e-mail `503`, papéis nenhum, caminhos `/auth/otp/*`, JSON `{ "email" }`, normalização lowercase, 404 (não 403) para id de outro usuário.

---

## Specific References

- Produto até agora: “App single-user, sem autenticação” ([docs/product.md](../../../docs/product.md)) — este feature substitui isso.
- Constraint atual: `uq_tracked_game_rawg_id` em `rawg_id` só ([V1__tracked_games_and_sessions.sql](../../../src/main/resources/db/migration/V1__tracked_games_and_sessions.sql)).
- UI atual: `fetch` sem credenciais ([frontend/src/api/apiClient.ts](../../../frontend/src/api/apiClient.ts)); rotas `/`, `/search`, `/games/:id` abertas ([frontend/src/App.tsx](../../../frontend/src/App.tsx)).
- Bruno: estudo do fluxo; Google; “método que manda um código via e-mail em vez da senha”.

---

## Deferred Ideas

- Senha tradicional, “esqueci senha”, tela de cadastro.
- GitHub / Apple / outros IdPs.
- 2FA, papéis, admin.
- Migrar jogos órfãos para o primeiro login.
- RFC 7807.
- Redis / session store compartilhado além do que o Design escolher para 7 dias.
