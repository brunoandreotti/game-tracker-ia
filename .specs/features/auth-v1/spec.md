# Auth v1 Specification

## Problem Statement

O diário hoje é single-user e qualquer cliente local chama a API. Bruno quer estudar o fluxo de autenticação de ponta a ponta: entrar sem senha (código de 6 dígitos por e-mail) e entrar com Google, com a mesma conta quando o e-mail coincidir, e com o diário isolado por usuário. Sem isso, login seria só um portão cosmético em cima de um banco compartilhado.

## Goals

- [ ] Completar o demo de estudo: deslogado → `/login` → código (Mailpit) ou Google → lista vazia daquela conta → acompanhar um jogo → outra conta não vê esse jogo → logout
- [ ] Exigir sessão em busca e diário (`401` sem sessão); unique de `rawgId` por usuário, não global
- [ ] UI em português na rota `/login` (dois passos) + logout no header, no tema dark / shadcn já adotado

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
| --- | --- |
| Senha, “esqueci senha”, tela de cadastro | Recorte é passwordless; o primeiro login cria o usuário |
| GitHub, Apple ou outros OAuth | Só Google neste passo |
| 2FA, papéis, admin | Estudo de login, não de autorização rica |
| Migrar jogos já gravados sem dono | Discussão Guided: cada conta nasce vazia |
| RFC 7807 (Problem Details) | Erros continuam `{ status, error, message }` |
| JWT vs cookie como tema de produto | Mecânica da sessão é Design; o spec exige só “sessão autenticada” observável |
| Redis, Spring Authorization Server (app como IdP) | Não pedido |
| Guia de estudo do frontend | Bruno pediu o arquivo só da parte backend |
| Mover backend para `api/` | AD-010 |

---

## Assumptions & Open Questions

Every ambiguity is resolved or logged here - nothing is left silently unclear.

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Dono do diário | Por usuário: lista e mutações só da sessão; API recusa anônimo | Discussão Guided. Senão o login não ensina autorização | y |
| Identidade | Mesmo e-mail (Google ou OTP) = a mesma conta e o mesmo diário | Discussão Guided. Padrão da indústria; estudo do vínculo | y |
| Cadastro | Não há tela de cadastro. Primeiro Google ou primeiro OTP bem-sucedido daquele e-mail cria o usuário | Discussão Guided | y |
| Dados pré-auth | Não migrar `tracked_game` existentes. Conta nova vê lista vazia | Discussão Guided (estudo) | y |
| Tela de login | Uma rota `/login` em dois passos: (1) Google ou e-mail + enviar código; (2) campo de 6 dígitos. Sucesso → `/` | Discussão Guided | y |
| Regras do OTP | 6 dígitos numéricos, 10 min, no máximo 5 erros (invalida o código), reenvio invalida o anterior | Discussão Guided | y |
| Entrega local do e-mail | Caixa de dev (Mailpit no Compose). Sem SMTP de produção neste passo | Discussão Guided. Código precisa ser lido no estudo | y |
| Senha | Ausente nesta feature | Bruno pediu código e Google, não senha | y |
| Sessão | Sobrevive a F5; dura **7 dias** desde o estabelecimento ou até logout, o que ocorrer primeiro | Premissa do plano; estudo sem “lembrar de mim” extra | n |
| Rate limit de envio | HTTP **429** se o mesmo e-mail pedir OTP de novo em menos de **60 s** | Premissa do plano; evita flood | n |
| Superfície protegida | `GET /games/search` e todos os `/tracked-games` exigem sessão; sem sessão → HTTP **401** e corpo `{ status, error, message }` | Premissa do plano; isolamento real | n |
| UI sem sessão | `/`, `/search`, `/games/:id` redirecionam para `/login` | Premissa do plano | n |
| Falha Google | Sem sessão; UI volta a `/login` com mensagem em português | Premissa do plano | n |
| Falha ao enviar e-mail | HTTP **503**, sem sessão, código não utilizável | Premissa do plano | n |
| Papéis | Nenhum. Todo usuário autenticado tem as mesmas permissões sobre o próprio diário | Premissa do plano | n |
| Outros OAuth | Só Google | Premissa do plano | n |
| Caminhos HTTP de OTP | `POST /auth/otp/request`, `POST /auth/otp/verify`, `POST /auth/logout`, `GET /auth/me` | Precisam ser testáveis; Design pode documentar equivalente Spring se o wiring exigir alias | n |
| Corpo OTP request | `{ "email": "user@example.com" }` | Um campo só | n |
| Corpo OTP verify | `{ "email": "user@example.com", "code": "123456" }` | E-mail + código | n |
| Sucesso OTP request | HTTP **204** sem body | Não devolve o código | n |
| Sucesso OTP verify / sessão | HTTP **200** em `/auth/me` e em verify; verify pode devolver o mesmo JSON de `/auth/me` | Cliente precisa saber que entrou | n |
| JSON de `/auth/me` e verify 200 | `{ "email": "user@example.com" }` (e-mail normalizado) | Mínimo para a UI | n |
| Logout | HTTP **204**. Sem sessão também **204** (idempotente) | Não vaza estado | n |
| E-mail válido | Após trim: não vazio, exatamente um `@`, partes local e domínio não vazias, sem espaços | 400 inequívoco; não RFC 5322 completo | n |
| Normalização de e-mail | Trim + lowercase antes de persistir e comparar (OTP e Google) | Mesmo e-mail = mesma conta | n |
| Google sem e-mail | Falha: sem sessão; equivalente a login Google mal-sucedido | Conta precisa de e-mail para o vínculo | n |
| URLs Google | Redirect OAuth 2 authorization-code. Paths exatos (`/oauth2/authorization/google`, callback) são Design (Spring Security) | Spec descreve resultado, não o adapter | n |
| Código no HTTP | Nunca no body de sucesso nem de erro | Evita vazar OTP nos testes de contrato e nos logs de response | n |
| 409 de `rawgId` | Só se **este** usuário já acompanha aquele `rawgId` | Consequência do diário por usuário | n |
| Id de outro usuário | GET/PATCH/DELETE de jogo ou sessão → HTTP **404** (não 403) | Não vaza que o id existe | n |
| Observabilidade | Log padrão do Boot. Sem métricas/tracing novos | N/A because o recorte é fluxo de login, não ops | n |
| Concorrência OTP | Last-write-wins no código vigente; o rate limit de 60 s reduz corrida de reenvio | N/A because uso local de estudo | n |
| Pagamentos | Não se aplica | N/A because não há cobrança | n |
| Guia de estudo backend | `docs/study/auth-v1-backend.md` gerado no **fim do Execute** (código real + teoria + fluxo). Não na Specify | Pedido do Bruno 2026-08-30; estudo; um arquivo só evita teoria solta | y |

**Open questions:** none - all resolved or logged above.

---

## User Stories

### P1: Entrar com código por e-mail ⭐ MVP

**User Story**: As a player, I want to sign in with a one-time code sent to my email so that I can use the diary without a password.

**Why P1**: Passwordless is half of the study flow; session is created here.

**Acceptance Criteria**:

1. WHEN the client sends `POST /auth/otp/request` with a valid `email` THEN the system SHALL return HTTP 204 and SHALL send a 6-digit numeric code to that email.
2. WHEN the client sends `POST /auth/otp/request` THEN the system SHALL NOT include the one-time code in the HTTP response body.
3. WHEN the client sends `POST /auth/otp/verify` with that email and the matching unexpired code THEN the system SHALL return HTTP 200 and a JSON object with `email`, and SHALL establish an authenticated session.
4. WHEN `POST /auth/otp/verify` succeeds for an email with no existing user THEN the system SHALL create a user for that email.
5. WHEN `POST /auth/otp/verify` succeeds for an email that already has a user THEN the system SHALL authenticate as that existing user and SHALL NOT create a second user for that email.
6. WHEN an authenticated client sends `GET /auth/me` THEN the system SHALL return HTTP 200 and a JSON object whose `email` is that user’s normalized email.
7. IF `email` on `POST /auth/otp/request` is missing, empty, only whitespace, or not a valid email THEN the system SHALL return HTTP 400 and a JSON body with `status`, `error`, and `message`, and SHALL NOT send a code.
8. IF `POST /auth/otp/verify` is sent with a missing code, a code that is not exactly 6 digits, an expired code, or a code that does not match the current OTP for that email THEN the system SHALL return HTTP 401 and a JSON body with `status`, `error`, and `message`, and SHALL NOT establish a session.
9. IF the client submits 5 incorrect codes for the current OTP THEN the system SHALL invalidate that OTP.
10. IF the client submits a code after that OTP was invalidated by too many failures THEN the system SHALL return HTTP 401 and SHALL NOT establish a session.
11. WHEN the client sends `POST /auth/otp/request` for an email that already has an unused OTP THEN the system SHALL invalidate the previous OTP and send a new 6-digit code.
12. IF the client sends `POST /auth/otp/request` for an email within 60 seconds of a previous successful OTP request for that same email THEN the system SHALL return HTTP 429 and a JSON body with `status`, `error`, and `message`, and SHALL NOT send a new code.
13. IF sending the email fails THEN the system SHALL return HTTP 503 and a JSON body with `status`, `error`, and `message`, and SHALL NOT establish a session.
14. WHEN 10 minutes have passed since an OTP was issued THEN the system SHALL reject that code on `POST /auth/otp/verify` with HTTP 401 and SHALL NOT establish a session.
15. WHEN an authenticated client sends `POST /auth/logout` THEN the system SHALL return HTTP 204 and SHALL end that session.
16. WHEN a client sends `POST /auth/logout` without a session THEN the system SHALL return HTTP 204.
17. WHILE a session is younger than 7 days and has not been logged out, WHEN the client sends a protected request with that session THEN the system SHALL NOT return HTTP 401 solely due to missing authentication.
18. WHEN 7 days have passed since a session was established THEN the system SHALL reject subsequent protected requests that used that session with HTTP 401.

**Independent Test**: Request OTP for a valid email (204, code only in the mail sink), verify (200 + session), `GET /auth/me` returns that email; wrong code 401; fifth failure then another verify 401 until a new request; second request within 60 s returns 429; logout then `GET /tracked-games` returns 401.

---

### P1: Entrar com Google ⭐ MVP

**User Story**: As a player, I want to sign in with Google so that I can reuse an existing Google account without a password.

**Why P1**: Second half of the study flow; must merge with OTP on the same email.

**Acceptance Criteria**:

1. WHEN Google sign-in completes successfully with an email THEN the system SHALL establish an authenticated session for that email.
2. WHEN Google sign-in succeeds for an email with no existing user THEN the system SHALL create a user for that email.
3. WHEN Google sign-in succeeds for an email that already has a user (created by OTP or by a prior Google sign-in) THEN the system SHALL authenticate as that existing user and SHALL NOT create a second user.
4. IF Google sign-in is cancelled or fails THEN the system SHALL NOT establish a session.
5. IF Google does not provide an email THEN the system SHALL NOT establish a session.

**Independent Test**: Complete Google sign-in for a new email, then `GET /auth/me` shows that email; complete Google for an email previously used via OTP and see the same diary; cancel Google and remain unauthenticated.

---

### P1: Diário isolado por usuário ⭐ MVP

**User Story**: As a player, I want my tracked games to belong only to my account so that another person who signs in does not see or change my diary.

**Why P1**: Without isolation, auth is only a gate in front of shared rows (`rawg_id` unique globally today).

**Acceptance Criteria**:

1. IF an unauthenticated client calls `GET /games/search` or any `/tracked-games` endpoint THEN the system SHALL return HTTP 401 and a JSON body with `status`, `error`, and `message`.
2. IF an unauthenticated client calls `GET /auth/me` THEN the system SHALL return HTTP 401 and a JSON body with `status`, `error`, and `message`.
3. WHEN an authenticated client sends `GET /tracked-games` THEN the system SHALL return HTTP 200 and only tracked games owned by that user.
4. WHEN user A tracks `rawgId` X and user B then tracks the same `rawgId` X THEN the system SHALL persist two tracked games (one per user) and SHALL NOT return HTTP 409 to B solely because A already tracks X.
5. WHEN an authenticated user already tracks `rawgId` X and that same user sends `POST /tracked-games` with the same `rawgId` THEN the system SHALL return HTTP 409 and a JSON body with `status`, `error`, and `message`.
6. IF an authenticated client sends GET, PATCH, or DELETE for `/tracked-games/{id}` or a session under that id when the tracked game is owned by a different user THEN the system SHALL return HTTP 404 and a JSON body with `status`, `error`, and `message`.
7. WHEN a user authenticates for the first time THEN `GET /tracked-games` SHALL return an empty JSON array even if the database still contains tracked games created before auth-v1.
8. The system SHALL enforce uniqueness of `rawgId` per user, not globally across all users.

**Independent Test**: Sign in as A, track a game, list shows it; sign in as B, list is empty, B can track the same `rawgId` (201); A requesting B’s id gets 404; anonymous `GET /tracked-games` is 401.

---

### P1: UI de login, sessão e logout ⭐ MVP

**User Story**: As a player, I want a Portuguese `/login` screen and a logout control so that I can complete the auth study flow without curl.

**Why P1**: Vertical slice; Bruno learns the browser flow, not only the API.

**Acceptance Criteria**:

1. WHEN an unauthenticated user opens `/`, `/search`, or `/games/:id` THEN the system SHALL redirect to `/login`.
2. WHEN the user opens `/login` unauthenticated THEN the system SHALL show a Google sign-in control and an email field with a send-code control, all labeled in Portuguese.
3. WHEN the user successfully requests a code from `/login` THEN the system SHALL show a 6-digit code field on the same route (second step) without navigating away from `/login`.
4. WHEN OTP verification succeeds from `/login` THEN the system SHALL navigate to `/`.
5. WHEN Google sign-in succeeds THEN the system SHALL navigate to `/`.
6. WHEN the user activates logout THEN the system SHALL call `POST /auth/logout` and THEN navigate to `/login`.
7. WHILE the user is authenticated THEN the system SHALL show a logout control in the header.
8. IF OTP verification fails THEN the system SHALL show an error message in Portuguese and SHALL remain on `/login`.
9. IF Google sign-in is cancelled or fails THEN the system SHALL return the user to `/login` and SHALL show an error message in Portuguese.
10. IF a diary or search API call returns HTTP 401 THEN the system SHALL navigate to `/login`.
11. WHEN an authenticated user opens `/login` THEN the system SHALL navigate to `/`.

**Independent Test**: Open `/` logged out → land on `/login`; request code, enter it, land on `/` with empty list; logout in header → `/login`; Google success → `/`; Google cancel → `/login` with error.

---

## Edge Cases

- IF `POST /auth/otp/verify` uses a valid code with a different email than the one that requested it THEN the system SHALL return HTTP 401 and SHALL NOT establish a session.
- IF the user requests a new OTP after a successful login (new request) THEN the previous session SHALL remain valid until logout or 7-day expiry (requesting a code does not log the user out).
- IF two users share no email THEN the system SHALL keep their tracked games disjoint.
- WHEN `coverUrl` / diary JSON shape is unchanged from library-v1 except ownership THEN the system SHALL keep the existing tracked-game and session JSON fields (`id`, `rawgId`, `name`, `year`, `coverUrl`, `status`, `rating`, `totalMinutes`, session `id` / `durationMinutes` / `playedAt`).
- IF the API is unreachable from the UI during login THEN the system SHALL show the existing Portuguese network error (or an equivalent login error in Portuguese) and SHALL NOT fabricate a session.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| AUTH-01 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-02 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-03 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-04 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-05 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-06 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-07 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-08 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-09 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-10 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-11 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-12 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-13 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-14 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-15 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-16 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-17 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-18 | P1: Entrar com código por e-mail | Design | Pending |
| AUTH-19 | P1: Entrar com Google | Design | Pending |
| AUTH-20 | P1: Entrar com Google | Design | Pending |
| AUTH-21 | P1: Entrar com Google | Design | Pending |
| AUTH-22 | P1: Entrar com Google | Design | Pending |
| AUTH-23 | P1: Entrar com Google | Design | Pending |
| AUTH-24 | P1: Diário isolado por usuário | Design | Pending |
| AUTH-25 | P1: Diário isolado por usuário | Design | Pending |
| AUTH-26 | P1: Diário isolado por usuário | Design | Pending |
| AUTH-27 | P1: Diário isolado por usuário | Design | Pending |
| AUTH-28 | P1: Diário isolado por usuário | Design | Pending |
| AUTH-29 | P1: Diário isolado por usuário | Design | Pending |
| AUTH-30 | P1: Diário isolado por usuário | Design | Pending |
| AUTH-31 | P1: Diário isolado por usuário | Design | Pending |
| AUTH-32 | P1: UI de login, sessão e logout | Design | Pending |
| AUTH-33 | P1: UI de login, sessão e logout | Design | Pending |
| AUTH-34 | P1: UI de login, sessão e logout | Design | Pending |
| AUTH-35 | P1: UI de login, sessão e logout | Design | Pending |
| AUTH-36 | P1: UI de login, sessão e logout | Design | Pending |
| AUTH-37 | P1: UI de login, sessão e logout | Design | Pending |
| AUTH-38 | P1: UI de login, sessão e logout | Design | Pending |
| AUTH-39 | P1: UI de login, sessão e logout | Design | Pending |
| AUTH-40 | P1: UI de login, sessão e logout | Design | Pending |
| AUTH-41 | P1: UI de login, sessão e logout | Design | Pending |
| AUTH-42 | P1: UI de login, sessão e logout | Design | Pending |

**Coverage:** 42 total, 0 mapped to tasks, 42 unmapped (Tasks phase not started).

---

## Success Criteria

- [ ] Demo: open the app logged out → `/login` → email code from Mailpit → empty list → track a game → second account does not see it → logout
- [ ] Same email via OTP then Google (or the reverse) opens the same diary
- [ ] Anonymous calls to search and tracked-games return HTTP 401
- [ ] No password, no extra OAuth providers, no migration of pre-auth rows into a user
- [ ] Guia de estudo do backend em [`docs/study/auth-v1-backend.md`](../../../docs/study/auth-v1-backend.md) escrito **após** o código (fluxo, motivo, classes, teoria)
