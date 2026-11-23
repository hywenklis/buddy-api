# Account Reset Password Specification

## Purpose

Definir o fluxo público de redefinição de senha (`POST /v1/accounts/password/reset`) utilizando um token de uso único previamente gerado pelo fluxo de recuperação de senha, garantindo atualização segura da credencial, consumo atômico do token e revogação global pós-commit de sessões anteriores.

## Requirements

### Requirement: Password Reset via Valid Token

O sistema SHALL permitir a redefinição de senha mediante um token válido e não expirado enviado para `POST /v1/accounts/password/reset`, atualizando o hash da senha da conta e revogando tokens anteriores após a confirmação da transação.

#### Scenario: Redefinição de senha bem-sucedida com token válido

- **WHEN** o cliente envia `POST /v1/accounts/password/reset` com um `token` válido e uma `newPassword` em conformidade com a política de senha
- **THEN** o sistema SHALL consumir atomicamente o `token` via `ForgotPasswordTokenManager.consumeToken`, codificar a nova senha com `PasswordEncoder`, persistir a atualização da conta e retornar `200 OK`
- **AND** o listener em `TransactionPhase.AFTER_COMMIT` SHALL invocar `TokenBlocklistService.revokeAllUserTokens` para invalidar tokens anteriores do usuário via timestamp no Redis.

### Requirement: Single-Use Atomic Reset Token Validation

O sistema SHALL garantir que cada token de redefinição seja consumido no máximo uma única vez de forma atômica, rejeitando tokens inválidos, expirados, já utilizados ou consumidos por requisição concorrente.

#### Scenario: Token inválido ou expirado

- **WHEN** o cliente envia um `token` inexistente ou expirado no Redis
- **THEN** o sistema SHALL lançar `NotFoundException` retornando `404 Not Found` para o campo `token` com a mensagem `"Invalid or expired reset token"`
- **AND** o sistema SHALL manter a senha inalterada e não invocar `TokenBlocklistService.revokeAllUserTokens`.

#### Scenario: Reutilização de token já consumido (single-use)

- **WHEN** o cliente envia uma segunda requisição utilizando o mesmo `token` após uma redefinição bem-sucedida
- **THEN** o sistema SHALL retornar `404 Not Found` para o campo `token` com a mensagem `"Invalid or expired reset token"`.

#### Scenario: Requisições concorrentes com o mesmo token

- **WHEN** duas requisições simultâneas tentam redefinir a senha usando o mesmo `token` válido
- **THEN** apenas uma requisição SHALL consumir o token com sucesso retornando `200 OK` e a outra SHALL retornar `404 Not Found`.

### Requirement: Reset Password Input and Policy Validation

O sistema SHALL validar na borda da camada web a presença do token e a conformidade da nova senha com `PasswordPolicy`.

#### Scenario: Token em branco ou ausente

- **WHEN** o campo `token` é nulo ou em branco em `ResetPasswordRequest`
- **THEN** o sistema SHALL rejeitar a requisição com `400 Bad Request` para o campo `token` e mensagem `"Reset token is mandatory"`.

#### Scenario: Nova senha fora da política de senha forte

- **WHEN** o campo `newPassword` não atende ao tamanho de 6 a 16 caracteres ou ao padrão `PasswordPolicy.STRONG_PASSWORD_REGEX` (maiúscula, minúscula, número e caractere especial)
- **THEN** o sistema SHALL rejeitar a requisição com `400 Bad Request` para o campo `newPassword`.

### Requirement: Reset Password Rate Limiting

O sistema SHALL proteger o endpoint `POST /v1/accounts/password/reset` contra abuso e força bruta utilizando `@RateLimited(useIp = true, operation = "resetPassword")`.

#### Scenario: Excesso de tentativas de redefinição de senha

- **WHEN** o cliente excede o limite de requisições configurado para a operação `resetPassword`
- **THEN** o sistema SHALL bloquear a requisição com `429 Too Many Requests` e a mensagem configurada na anotação `@RateLimited`.
