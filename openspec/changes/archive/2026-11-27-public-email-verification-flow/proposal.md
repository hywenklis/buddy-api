# Proposal

## Why

Atualmente, os endpoints de verificação de e-mail (`POST /v1/accounts/verifications/request` e `POST /v1/accounts/verifications/confirm`) exigem autenticação via JWT e o cadastro (`POST /v1/accounts/register`) não envia automaticamente o e-mail inicial de confirmação. Em aplicações web abertas como o Buddy, usuários frequentemente abrem o link de confirmação pelo celular ou após expiração da sessão (sem cookies), além de o `buddy-client` já redirecionar recém-cadastrados para `/auth/verification-pending?email=...` sem sessão ativa — gerando erro `403 Forbidden` em produção local.

## What Changes

- Disparar automaticamente o e-mail inicial de verificação pós-commit (`TransactionPhase.AFTER_COMMIT`) ao criar uma nova conta em `POST /v1/accounts/register`.
- **BREAKING**: Alterar `POST /v1/accounts/verifications/request` para endpoint público (`permitAll()`), aceitando `RequestEmailVerificationRequest` (`{ "email": "..." }`) com proteção `@RateLimited` e resposta `202 Accepted` anti-enumeração (sem revelar se a conta existe, está indisponível ou já foi verificada).
- **BREAKING**: Alterar `POST /v1/accounts/verifications/confirm` para endpoint público (`permitAll()`), aceitando `ConfirmEmailRequest` (`{ "token": "<uuid>" }`) com consumo atômico de uso único no Redis (`GETDEL` / Lua fallback), `@RateLimited` por IP e retorno `200 OK` (ou `404 Not Found` para token inválido/expirado/já consumido).

## Capabilities

### New Capabilities
- `account/email-verification`: Disparo automático de verificação pós-cadastro, reenvio público anti-enumeração e confirmação pública atômica de uso único via token.

### Modified Capabilities
- (Nenhuma capability existente em `openspec/specs/` possuía contrato prévio de verificação de e-mail; os links dos templates já estão cobertos por `account/email-links`).

## Impact

- **Camadas / Bounded Contexts impactados**: `domains.account` (`CreateAccountImpl`), `domains.account.email` (`EmailVerificationService`, `EmailVerificationServiceImpl`), `commons.configurations.cache` (`TokenManager`), `commons.configurations.security` (`SecurityConfig`), `web.accounts` (`EmailVerificationController`, `EmailVerificationControllerDoc`, `RequestEmailVerificationRequest`) e testes em `units/` e `integrations/`.
- **Integração com `buddy-client`**: Alinha diretamente com `/auth/register`, `/auth/verification-pending` e `/auth/verify-email` sem necessidade de o usuário fazer login antes de confirmar seu e-mail.

## Fora de Escopo (Out of Scope)

- Alterações nas URLs ou templates HTML de e-mail (já especificados em `account/email-links`).
- Criação automática de sessão ou emissão de cookies JWT no momento do cadastro.
- Alterações em fluxos de recuperação/redefinição de senha (`account/reset-password`) ou seed de `terms_version`.
