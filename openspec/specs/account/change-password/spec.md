# Account Change Password Specification

## Purpose

Definir o fluxo autenticado de alteração de senha (`PATCH /v1/accounts/password`) permitindo que um usuário logado altere sua própria senha mediante verificação da credencial atual, com limpeza de cookies, revogação global de tokens, registro de auditoria de segurança conforme LGPD e notificação por e-mail pós-commit.

## Requirements

### Requirement: Authenticated Password Change

O sistema SHALL permitir que um usuário autenticado altere sua senha via `PATCH /v1/accounts/password` após validar a senha atual e persistir o novo hash codificado, limpando os cookies de autenticação na resposta.

#### Scenario: Alteração de senha autenticada bem-sucedida

- **WHEN** um usuário autenticado envia `PATCH /v1/accounts/password` com `currentPassword` compatível com o hash armazenado e `newPassword` válida e distinta da atual
- **THEN** o sistema SHALL codificar `newPassword` com `PasswordEncoder`, atualizar a senha da conta, remover os cookies via `@ClearCookiesOnSuccess` e retornar `204 No Content`.

### Requirement: Current Password and Non-Reuse Validation

O sistema SHALL validar que a conta existe, que `currentPassword` confere com o hash atual e que `newPassword` não reutiliza a senha atual antes de efetivar a alteração ou disparar eventos pós-commit.

#### Scenario: Senha atual incorreta

- **WHEN** `currentPassword` não corresponde ao hash armazenado da conta autenticada
- **THEN** o sistema SHALL lançar `InvalidCurrentPasswordException` retornando `400 Bad Request` no campo `currentPassword` com mensagem `"Invalid current password"`
- **AND** o sistema SHALL não atualizar a senha nem publicar `PasswordChangedEvent`.

#### Scenario: Nova senha idêntica à senha atual

- **WHEN** `newPassword` corresponde ao hash atual da conta
- **THEN** o sistema SHALL lançar `DomainException` com status `400 Bad Request` no campo `newPassword` e mensagem `"New password cannot be the same as current password"`
- **AND** o sistema SHALL não atualizar a senha nem publicar `PasswordChangedEvent`.

#### Scenario: Conta autenticada não encontrada

- **WHEN** `FindAccount.findById` não localiza a conta associada ao usuário autenticado
- **THEN** o sistema SHALL propagar `NotFoundException` e não executar atualização nem publicar `PasswordChangedEvent`.

### Requirement: Change Password Input Policy and Authentication

O sistema SHALL exigir autenticação válida e aplicar validação Bean Validation na borda sobre `ChangePasswordRequest` antes de acionar a camada de domínio.

#### Scenario: Requisição sem autenticação válida

- **WHEN** um cliente não autenticado ou utilizando token revogado envia `PATCH /v1/accounts/password`
- **THEN** a camada de segurança SHALL bloquear o acesso antes de executar `ChangePasswordController`.

#### Scenario: Campos obrigatórios ausentes ou nova senha fraca

- **WHEN** `currentPassword` ou `newPassword` está em branco, fora de 6 a 16 caracteres ou em desacordo com `PasswordPolicy.STRONG_PASSWORD_REGEX`
- **THEN** o sistema SHALL retornar `400 Bad Request` indicando a violação de validação no respectivo campo.

### Requirement: Post-Commit Token Revocation, Security Audit, and Notification

O sistema SHALL executar exclusivamente após o commit da transação (`TransactionPhase.AFTER_COMMIT`) a revogação global de sessões, a gravação do evento de auditoria com conformidade LGPD em transação independente (`Propagation.REQUIRES_NEW`) e o envio da notificação por e-mail.

#### Scenario: Efeitos colaterais pós-commit após alteração bem-sucedida

- **WHEN** a transação de alteração de senha é confirmada com sucesso e `ChangePasswordServiceImpl.handlePasswordChanged` processa `PasswordChangedEvent`
- **THEN** o sistema SHALL revogar todos os tokens anteriores do usuário via `TokenBlocklistService.revokeAllUserTokens` (tornando inválido inclusive o JWT da requisição atual)
- **AND** o sistema SHALL persistir um registro `SecurityEventType.PASSWORD_CHANGED` na tabela `security_audit_event` via `SecurityAuditService` em nova transação (`Propagation.REQUIRES_NEW`) com endereço IP criptografado por `IpAddressEncryptor`, sem alterar `TermsAcceptanceEntity`
- **AND** o sistema SHALL enfileirar a notificação por e-mail ao usuário via `EmailSender.dispatchPasswordChangedNotification`.

### Requirement: Change Password Rate Limiting

O sistema SHALL proteger `PATCH /v1/accounts/password` com `@RateLimited(useIp = true, operation = "password-change", emailSpel = "#userDetails.username")` usando buckets independentes por operação/e-mail e operação/IP.

#### Scenario: Excesso de tentativas de alteração de senha

- **WHEN** o usuário autenticado ou IP excede o limite de requisições da operação `password-change`
- **THEN** o sistema SHALL rejeitar a chamada com `429 Too Many Requests`.
