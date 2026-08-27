# Ainda em aberto

Itens para o agente **perguntar e sugerir** durante o planejamento. Não tratar como decidido até confirmar e mover para [product.md](product.md) ou [stack.md](stack.md).

## Produto / API

- Contrato JSON (bodies de search, library, sessions; campos de horas totais).
- Erros HTTP: 404 (entrada/sessão inexistente), 409 (jogo já na biblioteca), 400 (nota fora de 1–10, minutos ≤ 0).
- `playedAt` obrigatório ou default “agora”? Fuso?
- Sessão tem nota/comentário ou só duração + data?
- `GET /library` filtra por status?

## Stack

- Flyway vs Hibernate `ddl-auto` no v1.
- `spring-boot-starter-validation` no v1, sim ou não.
- Testcontainers nos testes de persistência agora ou depois.
- Layout Compose: um `compose.yaml` na raiz vs pasta `docker/`.
- Feign: BOM Cloud estável para Boot 4.1.1 (gate); senão `@HttpExchange`.

## Processo

- O que mais você quiser puxar para o v1 ou deixar explícito como “depois”.
