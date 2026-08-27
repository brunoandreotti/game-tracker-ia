# Produto

## O que é

Um **diário pessoal de jogos eletrônicos**: o que você jogou, quanto jogou, em que momento da jornada está, e o que achou.

Não é rede social nem clone da Steam.

## Recorte do v1

Três conceitos:

| Conceito | Papel |
|---|---|
| **Jogo** | Vem do RAWG (id, nome, ano, capa). Cópia local só quando você começa a acompanhar. A busca **não** grava. |
| **Jogo acompanhado** | Relacionamento com o jogo: status + nota. Um `rawgId` = no máximo um registro. |
| **Sessão** | “Joguei X minutos neste dia”. Horas totais = **soma das sessões**. |

**Status:** `WANT_TO_PLAY` / `PLAYING` / `COMPLETED` / `DROPPED`. Qualquer um dos quatro pode ser gravado a partir de qualquer status atual (POST e PATCH). Sem máquina de estados.

**Nota:** inteiro 1–10, opcional até avaliar.

**Sessão:** `durationMinutes` + `playedAt`.

App **single-user**, sem autenticação (uso local). Sem UI: HTTP client / `curl`.

`POST /tracked-games`: status inicial opcional; se omitido, default **`PLAYING`**.

v1 inclui **DELETE** de sessão e de jogo acompanhado (corrigir erro / parar de acompanhar).

## API (especificação, não implementada)

IDs de jogo acompanhado e sessão: `Long` sequencial. Horas totais no JSON: `totalMinutes` (inteiro). “2h30” no demo é leitura humana, não o campo.

`GET /tracked-games` lista tudo; **sem** filtro por status no v1. Sem paginação.

Sessão é só `durationMinutes` + `playedAt` (sem nota/comentário). `playedAt` é data (`YYYY-MM-DD`), sem hora nem fuso; se omitido no create, default **hoje** (relógio do servidor).

Cópia do jogo: snapshot na hora do `POST /tracked-games` (nome, ano, capa). Mudança no RAWG depois **não** atualiza o registro.

### Endpoints

- `GET /games/search?q=` — busca no RAWG, não persiste
- `POST /tracked-games` — começa a acompanhar pelo `rawgId`; status opcional (default `PLAYING`)
- `GET /tracked-games` — lista com status, nota, `totalMinutes`
- `GET /tracked-games/{id}` — um jogo acompanhado; mesmo JSON do item da lista
- `PATCH /tracked-games/{id}` — status e/ou nota
- `DELETE /tracked-games/{id}` — para de acompanhar (e apaga as sessões)
- `POST /tracked-games/{id}/sessions` — `durationMinutes`, `playedAt` opcional
- `GET /tracked-games/{id}/sessions` — histórico daquele jogo
- `DELETE /tracked-games/{id}/sessions/{sessionId}` — remove a sessão (`totalMinutes` recalcula)

### Contrato JSON

Busca:

```http
GET /games/search?q=zelda
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
- `PATCH /tracked-games/{id}`: `{ "status"?: "...", "rating"?: 9 }` — pelo menos um campo. `rating: null` **não** limpa nota no v1 (sem “desavaliar”).
- `POST /tracked-games/{id}/sessions`: `{ "durationMinutes": 90, "playedAt": "2026-08-27" }` — `playedAt` opcional = hoje. Create `201`.
- `GET /tracked-games/{id}/sessions`: `[{ "id": 1, "durationMinutes": 90, "playedAt": "2026-08-27" }]`.
- `DELETE` de jogo acompanhado ou sessão: `204` sem body.

### Erros

Corpo: `{ "status", "error", "message" }` (sem RFC 7807 no v1).

| HTTP | Quando |
|---|---|
| 400 | Validação: `q` vazio, nota fora de 1–10, `durationMinutes` ≤ 0, PATCH sem campos, status inválido |
| 404 | Jogo acompanhado ou sessão inexistente; `rawgId` não existe no RAWG no `POST /tracked-games` |
| 409 | `rawgId` já está sendo acompanhado |
| 502 / 503 | RAWG indisponível na busca ou no add |

## Fora do v1

- UI / frontend
- Cadastro, login, vários usuários
- Import Steam, achievements, ranks
- Review longa, plataforma (PC/Switch/PS5), tags
- Estatísticas (horas no mês, média de nota, streaks)
- Filtro de `GET /tracked-games` por status, paginação
- RFC 7807 (Problem Details); limpar nota (`rating: null`)

## Depois do v1 (ordem)

1. Notas/plataforma — review curta e em qual console/PC
2. UI simples — lista + logar sessão
3. Números — totais por status, horas no período, zerados no ano
4. Integrações extras — Steam opcional; RAWG continua o catálogo

## Pronto quando

Só com a API: buscar “Zelda” no RAWG → acompanhar como `PLAYING` → duas sessões (90 min + 60 min) → listar com **`totalMinutes`: 150** (2h30) → nota 9 e status `COMPLETED`.
