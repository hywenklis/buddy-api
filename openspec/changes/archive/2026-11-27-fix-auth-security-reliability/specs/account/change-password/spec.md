## MODIFIED Requirements

### Requirement: Post-Commit Token Revocation, Security Audit, and Notification

O sistema SHALL executar exclusivamente após o commit da transação (`TransactionPhase.AFTER_COMMIT`) a revogação global de sessões, a gravação do evento de auditoria com conformidade LGPD em transação independente (`Propagation.REQUIRES_NEW`) e o envio da notificação por e-mail.

#### Scenario: Efeitos colaterais pós-commit após alteração bem-sucedida

- **WHEN** a transação de alteração de senha é confirmada com sucesso e `ChangePasswordServiceImpl.handlePasswordChanged` processa `PasswordChangedEvent`
- **THEN** o sistema SHALL revogar todos os tokens anteriores do usuário via `TokenBlocklistService.revokeAllUserTokens` (tornando inválido inclusive o JWT da requisição atual)
- **AND** o sistema SHALL persistir um registro `SecurityEventType.PASSWORD_CHANGED` na tabela `security_audit_event` via `SecurityAuditService` em nova transação (`Propagation.REQUIRES_NEW`) com endereço IP criptografado por `IpAddressEncryptor`, sem alterar `TermsAcceptanceEntity`
- **AND** o sistema SHALL enfileirar a notificação por e-mail ao usuário via `EmailSender.dispatchPasswordChangedNotification`.
