# Produto

## O que é

Um **diário pessoal de jogos eletrônicos**: o que você jogou, quanto jogou, em que momento da jornada está, e o que achou.

Não é rede social nem clone da Steam.

## Recorte do v1

Três conceitos:

| Conceito | Papel |
|---|---|
| **Jogo** | Vem do RAWG (id, nome, ano, capa). Cache local só quando você adiciona à biblioteca. A busca **não** persiste. |
| **Entrada na biblioteca** | Relacionamento com o jogo: status + nota. Um `rawgId` = no máximo uma entrada. |
| **Sessão** | “Joguei X minutos neste dia”. Horas totais da entrada = **soma das sessões**. |

**Status:** `WANT_TO_PLAY` / `PLAYING` / `COMPLETED` / `DROPPED`.

**Nota:** inteiro 1–10, opcional até avaliar.

**Sessão:** `durationMinutes` + `playedAt`.

App **single-user**, sem autenticação (uso local). Sem UI: HTTP client / `curl`.

`POST /library`: status inicial opcional; se omitido, default **`PLAYING`**.

v1 inclui **DELETE** de sessão e de entrada na biblioteca (corrigir erro / tirar jogo).

## API (especificação, não implementada)

- `GET /games/search?q=` — busca no RAWG, não persiste
- `POST /library` — adiciona pelo `rawgId`; status opcional (default `PLAYING`)
- `GET /library` — lista com status, nota, horas totais
- `PATCH /library/{id}` — status e/ou nota
- `DELETE /library/{id}` — remove a entrada (e as sessões dela)
- `POST /library/{id}/sessions` — `durationMinutes`, `playedAt`
- `GET /library/{id}/sessions` — histórico daquele jogo
- `DELETE /library/{id}/sessions/{sessionId}` — remove a sessão (horas totais recalculam)

## Fora do v1

- UI / frontend
- Cadastro, login, vários usuários
- Import Steam, achievements, ranks
- Review longa, plataforma (PC/Switch/PS5), tags
- Estatísticas (horas no mês, média de nota, streaks)

## Depois do v1 (ordem)

1. Notas/plataforma — review curta e em qual console/PC
2. UI simples — biblioteca + logar sessão
3. Números — totais por status, horas no período, zerados no ano
4. Integrações extras — Steam opcional; RAWG continua o catálogo

## Pronto quando

Só com a API: buscar “Zelda” no RAWG → adicionar como `PLAYING` → duas sessões (90 min + 60 min) → ver **2h30** totais → nota 9 e status `COMPLETED`.
