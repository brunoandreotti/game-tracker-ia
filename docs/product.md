# Produto

## O que é

Um **diário pessoal de jogos eletrônicos**: o que você jogou, quanto jogou, em que momento da jornada está, e o que achou.

Não é rede social nem clone da Steam.

## Recorte do v1

Três conceitos:

| Conceito | Papel |
|---|---|
| **Jogo** | Vem do RAWG (id, nome, ano, capa). Cópia local só quando você começa a acompanhar. A busca **não** grava. |
| **Jogo acompanhado** | Relacionamento do **usuário autenticado** com o jogo: status + nota. Um `rawgId` = no máximo um registro **por usuário**. |
| **Sessão** | “Joguei X minutos neste dia”. Horas totais = **soma das sessões**. |

**Status:** `WANT_TO_PLAY` / `PLAYING` / `COMPLETED` / `DROPPED`. Qualquer um dos quatro pode ser gravado a partir de qualquer status atual (POST e PATCH). Sem máquina de estados.

**Nota:** inteiro **0–5**, opcional até avaliar (`null` = sem nota). Escala antiga 1–10 supersedida (AD-014).

**Sessão:** `durationMinutes` + `playedAt`.

App **multi-user** com autenticação de estudo (spec [auth-v1](../.specs/features/auth-v1/spec.md)): **sem senha**. Entrar com **Google** ou **código de 6 dígitos por e-mail**; mesmo e-mail = mesma conta. Diário isolado por usuário. Primeiro login cria o usuário (sem tela de cadastro). Jogos gravados antes desta feature **não** são migrados.

`POST /tracked-games`: status inicial opcional; se omitido, default **`PLAYING`**.

API v1 inclui **DELETE** de sessão e de jogo acompanhado (corrigir erro / parar de acompanhar).

## UI v1 (planejada)

SPA em [`frontend/`](../frontend/) (React + Vite + TypeScript). Consome a API local; UI em português; visual de diário **dark** (Letterboxd-like, AD-012). Estudo de componentes: **shadcn/ui + Tailwind** (AD-013).

| Rota | Papel |
|---|---|
| `/login` | Entrar (Google ou e-mail + código); dois passos na mesma rota (auth-v1) |
| `/` | Lista de jogos **desta conta** (nota em estrelas); exige sessão |
| `/search` | Busca RAWG + acompanhar; exige sessão |
| `/games/:id` | Detalhe: status, nota (Select 0–5), sessões (criar/apagar), apagar acompanhamento; exige sessão |

Happy path na UI: `/login` → lista (vazia na conta nova) → buscar → acompanhar → detalhe → sessões → nota/status. Specs: [ui-v1](../.specs/features/ui-v1/spec.md), [auth-v1](../.specs/features/auth-v1/spec.md). Logout no header. Sem sessão, `/`, `/search` e `/games/:id` vão para `/login`.

Sem mover o backend para `api/` neste passo. CORS e `VITE_API_URL` na implementação.

## API v1

IDs de jogo acompanhado e sessão: `Long` sequencial. Horas totais no JSON: `totalMinutes` (inteiro). “2h30” no demo é leitura humana, não o campo.

`GET /tracked-games` lista só os jogos **do usuário da sessão**; **sem** filtro por status no v1. Sem paginação. Sem sessão: HTTP **401**.

Sessão é só `durationMinutes` + `playedAt` (sem nota/comentário). `playedAt` é data (`YYYY-MM-DD`), sem hora nem fuso; se omitido no create, default **hoje** (relógio do servidor).

Cópia do jogo: snapshot na hora do `POST /tracked-games` (nome, ano, capa). Mudança no RAWG depois **não** atualiza o registro.

### Auth (auth-v1)

- `POST /auth/otp/request` — `{ "email" }`; **204**; envia código de 6 dígitos (10 min, 5 erros, reenvio invalida o anterior). Rate limit: **429** se o mesmo e-mail pedir de novo em menos de 60 s. Falha de e-mail: **503**.
- `POST /auth/otp/verify` — `{ "email", "code" }`; **200** `{ "email" }` + sessão. Código errado/expirado: **401**.
- `GET /auth/me` — **200** `{ "email" }` com sessão; **401** sem.
- `POST /auth/logout` — **204** (também sem sessão).
- Google: redirect OAuth; sucesso cria/reutiliza usuário pelo e-mail e estabelece sessão. Paths exatos do redirect: Design.

Sessão: sobrevive a F5; **7 dias** ou até logout.

### Endpoints

- `GET /games/search?q=` — busca no RAWG (`search_precise` sempre ligado), não persiste. `exact=true` opcional mapeia para `search_exact`. **Exige sessão.**
- `POST /tracked-games` — começa a acompanhar pelo `rawgId` **nesta conta**; status opcional (default `PLAYING`)
- `GET /tracked-games` — lista **desta conta** com status, nota, `totalMinutes`
- `GET /tracked-games/{id}` — um jogo acompanhado **desta conta**; mesmo JSON do item da lista
- `PATCH /tracked-games/{id}` — status e/ou nota
- `DELETE /tracked-games/{id}` — para de acompanhar (e apaga as sessões)
- `POST /tracked-games/{id}/sessions` — `durationMinutes`, `playedAt` opcional
- `GET /tracked-games/{id}/sessions` — histórico daquele jogo
- `DELETE /tracked-games/{id}/sessions/{sessionId}` — remove a sessão (`totalMinutes` recalcula)

### Contrato JSON

Busca:

```http
GET /games/search?q=zelda
GET /games/search?q=Lies%20Of%20P&exact=true
```

```json
[{ "rawgId": 123, "name": "The Legend of Zelda: Breath of the Wild", "year": 2017, "coverUrl": "https://..." }]
```

Jogo acompanhado — create `201`; get by id / list / patch `200`:

```json
{
  "id": 1,
  "rawgId": 123,
  "name": "The Legend of Zelda: Breath of the Wild",
  "year": 2017,
  "coverUrl": "https://...",
  "status": "PLAYING",
  "rating": null,
  "totalMinutes": 0
}
```

- `POST /tracked-games`: `{ "rawgId": 123, "status": "PLAYING" }` — `status` opcional.
- `GET /tracked-games/{id}`: o mesmo objeto; `404` se o id não existir.
- `PATCH /tracked-games/{id}`: `{ "status"?: "...", "rating"?: 5 }` — pelo menos um campo. `rating` válido: **0–5**. `rating: null` **não** limpa nota no v1 (sem “desavaliar”).
- `POST /tracked-games/{id}/sessions`: `{ "durationMinutes": 90, "playedAt": "2026-08-27" }` — `playedAt` opcional = hoje. Create `201`.
- `GET /tracked-games/{id}/sessions`: `[{ "id": 1, "durationMinutes": 90, "playedAt": "2026-08-27" }]`.
- `DELETE` de jogo acompanhado ou sessão: `204` sem body.

### Erros

Corpo: `{ "status", "error", "message" }` (sem RFC 7807 no v1).

| HTTP | Quando |
|---|---|
| 400 | Validação: `q` vazio, nota fora de 0–5, `durationMinutes` ≤ 0, PATCH sem campos, status inválido, e-mail de OTP inválido |
| 401 | Sem sessão (busca/diário/`GET /auth/me`); código OTP errado, expirado ou invalidado |
| 404 | Jogo acompanhado ou sessão inexistente **nesta conta** (id de outro usuário também 404); `rawgId` não existe no RAWG no `POST /tracked-games` |
| 409 | Este usuário já acompanha esse `rawgId` |
| 429 | Novo OTP para o mesmo e-mail em menos de 60 s |
| 502 / 503 | RAWG indisponível na busca ou no add; **503** se o envio do e-mail OTP falhar |

## Fora da API v1 / UI v1 / auth-v1

- Senha, “esqueci senha”, tela de cadastro, GitHub/Apple, 2FA, papéis/admin
- Import Steam, achievements, ranks
- Review longa, plataforma (PC/Switch/PS5), tags
- Estatísticas (horas no mês, média de nota, streaks)
- Filtro de `GET /tracked-games` por status, paginação
- RFC 7807 (Problem Details); limpar nota (`rating: null`)
- Next.js, Tailwind/design system, PWA, app mobile
- Monorepo `api/` + `frontend/` (adiado; só `frontend/` na raiz por agora)

## Depois (ordem)

1. Auth v1 — spec/context escritos; Design → Tasks → Execute quando pedido ([auth-v1](../.specs/features/auth-v1/spec.md))
2. Notas/plataforma — review curta e em qual console/PC
3. Números — totais por status, horas no período, zerados no ano
4. Integrações extras — Steam opcional; RAWG continua o catálogo

## Pronto quando

**API v1:** buscar “Zelda” no RAWG → acompanhar como `PLAYING` → duas sessões (90 min + 60 min) → listar com **`totalMinutes`: 150** (2h30) → nota **5** e status `COMPLETED`.

**UI v1:** o mesmo demo só pela interface em `frontend/` (após login, auth-v1).

**Auth v1:** deslogado → `/login` → código (Mailpit) ou Google → lista vazia da conta → acompanhar → outra conta não vê o jogo → logout. Mesmo e-mail nos dois métodos = o mesmo diário.
