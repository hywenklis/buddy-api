# Account Email Links Alignment Specification

## Purpose

Garantir que as URLs configuradas para os templates de verificação de e-mail e recuperação de senha no `buddy-api` correspondam às rotas canônicas do `buddy-client` (`/auth/verify-email` e `/auth/reset-password`).

## Requirements

### Requirement: Canonical Client URLs in Email Template Properties
As propriedades `buddy.email.templates.verification.url` e `buddy.email.templates.forgot-password.url` definidas em `application-local.yml` e `application-test.yml` MUST apontar para `https://buddyclient.vercel.app/auth/verify-email?token=` e `https://buddyclient.vercel.app/auth/reset-password?token=`, garantindo links funcionais para o frontend quando concatenados ao token por `EmailSenderImpl`.

#### Scenario: Link de verificação de e-mail gerado com rota canônica
- **WHEN** `EmailSenderImpl.dispatchVerificationEmail` monta o link de confirmação usando `EmailProperties.templates().verification().url()`
- **THEN** a URL base utilizada SHALL ser `https://buddyclient.vercel.app/auth/verify-email?token=`
- **AND** o link final gerado no template SHALL corresponder a `https://buddyclient.vercel.app/auth/verify-email?token=<token>`.

#### Scenario: Link de recuperação de senha gerado com rota canônica
- **WHEN** `EmailSenderImpl.dispatchPasswordRecoveryEmail` monta o link de redefinição usando `EmailProperties.templates().forgotPassword().url()`
- **THEN** a URL base utilizada SHALL ser `https://buddyclient.vercel.app/auth/reset-password?token=`
- **AND** o link final gerado no template SHALL corresponder a `https://buddyclient.vercel.app/auth/reset-password?token=<token>`.

#### Scenario: Consistência entre `application-local.yml` e `application-test.yml`
- **WHEN** a aplicação ou a suíte de integração carrega `EmailProperties` com o perfil `local` ou `test`
- **THEN** ambas as configurações MUST expor exatamente os sufixos `/auth/verify-email?token=` e `/auth/reset-password?token=` sem referências legadas a `/confirm-email?token=` ou `/reset-password?token=` na raiz.
