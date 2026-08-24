# Planejamento: Alteração de Senha Autenticada (Issue #309)

## 1. Visão Geral
Implementar o endpoint `PATCH /v1/accounts/password` para permitir que um usuário logado altere sua própria senha, com forte camada de segurança, auditoria e revogação de tokens.

## 2. Análise de Domínio e Auditoria
Após analisar o código, a única tabela que hoje armazena informações de auditoria (IP e User-Agent) é a `TermsAcceptanceEntity`.
**Decisão Arquitetural sobre o IP:** 
- A tabela `TermsAcceptanceEntity` é um recibo legal de consentimento (LGPD). É uma boa prática de engenharia manter o IP e o User-Agent diretamente atrelados a esse recibo de forma atômica e imutável. Portanto, **não substituiremos** os campos dessa tabela pela nova tabela genérica.
- Em vez disso, criaremos a `SecurityAuditEventEntity` para focar em **eventos de ciclo de vida de conta e segurança** (Troca de Senha, Falha de Login, Bloqueio de Conta, etc). 
- **Importante:** O IP na nova tabela `SecurityAuditEventEntity` continuará sendo criptografado usando o nosso `IpAddressEncryptor` existente, garantindo conformidade com a LGPD.

## 3. Fluxo de Execução (O Padrão de Mercado Seguro)
1. **Requisição:** O cliente envia `currentPassword` e `newPassword`.
2. **Validação:** A API valida se o `currentPassword` bate com o hash salvo no banco. (Isso barra a ameaça do "computador destravado").
3. **Alteração:** A API codifica o `newPassword` e salva no banco.
4. **Revogação (Logoff global):** A API invoca o `TokenBlocklistService.revokeAllUserTokens(email)` já existente no projeto, o que automaticamente coloca os tokens antigos no Redis Blocklist. O usuário precisará fazer login novamente (até no dispositivo atual).
5. **Auditoria:** Salva um registro em `SecurityAuditEventEntity` com evento `PASSWORD_CHANGED`, pegando o IP e User-Agent vindos dos headers da requisição.
6. **Notificação:** Envia um e-mail através do `ManagerService` informando: *"Sua senha foi alterada com sucesso. Se não foi você, contate o suporte imediatamente."*

## 4. Tarefas e Componentes a Alterar

### 4.1. Banco de Dados e Entidades (Domínio de Auditoria)
- Criar o novo domínio `com.buddy.api.domains.audit`.
- Criar `SecurityAuditEventEntity` mapeada para a tabela `security_audit_event`.
  - Campos: `id` (UUID), `accountId` (UUID), `eventType` (String/Enum), `ipAddress` (String com `@Convert(converter = IpAddressEncryptor.class)`), `userAgent` (String), `createdAt` (LocalDateTime).
- Criar `SecurityAuditEventRepository` dentro do domínio `audit`.
- **Obrigatório:** Criar script Flyway (ex: `V123__create_table_security_audit_event.sql`) para a tabela de auditoria.

### 4.2. DTOs e Mappers (Web Layer e Domain Layer)
- **Web Layer:** Criar `ChangePasswordRequest` em `com.buddy.api.web.accounts.requests`.
- **Web Layer:** Criar `ChangePasswordMapperRequest` (usando MapStruct) em `com.buddy.api.web.accounts.mappers` para converter o Request no DTO de domínio.
- **Domain Layer:** Criar `ChangePasswordDto` em `com.buddy.api.domains.account.dtos`.

### 4.3. Controller (Web Layer)
- Criar a interface de documentação `ChangePasswordControllerDoc` (Swagger/OpenAPI).
- Criar o `ChangePasswordController` em `com.buddy.api.web.accounts.controllers` implementando a Doc, com o método `PATCH /v1/accounts/password`.
- O Controller extrairá os dados (`currentPassword`, `newPassword`), além do `X-Forwarded-For` (ou `HttpServletRequest.getRemoteAddr()`) e `User-Agent`, e os repassará via DTO para a camada de serviço.

### 4.4. Service (Domain Layer)
- **Domínio Audit:** Criar `SecurityAuditService` (Interface + Impl) em `com.buddy.api.domains.audit.services` para encapsular a lógica de gravação no repository.
- **Domínio Account:** Criar interface e implementação para a troca de senha (ex: `ChangePasswordService`) em `com.buddy.api.domains.account.password.services`.
  - Injetar dependências: `AccountRepository` (pois estamos no mesmo domínio), `PasswordEncoder`, `TokenBlocklistService`, `ManagerService`, e `SecurityAuditService` (chamada inter-domínio).

### 4.5. Testes (TDD/Cobertura)
- Seguir regra do projeto: Usar `AssertJ` e `@DisplayName`. Sem comentários.
- Testar falha com `currentPassword` errado (esperado erro de validação).
- Testar troca com sucesso:
  - Garantir que a senha nova foi salva (codificada).
  - Garantir que `revokeAllUserTokens` foi chamado.
  - Garantir que a auditoria foi gravada.
  - Garantir (via WireMock) que o e-mail de notificação foi enfileirado para envio (usar `waitUntilWireMockReceives(1)` para evitar falsos positivos).
