## ADDED Requirements

### Requirement: Automatic Verification Email Dispatch on Account Registration

O sistema SHALL gerar token de verificação e enfileirar o envio do e-mail de confirmação exclusivamente após o commit da transação (`TransactionPhase.AFTER_COMMIT`) ao criar uma nova conta via `POST /v1/accounts/register`.

#### Scenario: Cadastro bem-sucedido dispara e-mail de verificação pós-commit

- **WHEN** um cliente envia `POST /v1/accounts/register` com dados válidos e a transação é confirmada
- **THEN** o sistema SHALL retornar `201 Created`
- **AND** o listener `AFTER_COMMIT` SHALL gerar um token UUID no cache `emailVerificationToken` e enfileirar o envio do e-mail de verificação com o link canônico de confirmação.

#### Scenario: Falha de validação ou e-mail duplicado não dispara e-mail

- **WHEN** `POST /v1/accounts/register` falha por e-mail já cadastrado ou erro de validação Bean Validation
- **THEN** a transação SHALL sofrer rollback e nenhum token ou e-mail de verificação SHALL ser disparado.

### Requirement: Public Anti-Enumeration Email Verification Resend

O sistema SHALL expor `POST /v1/accounts/verifications/request` como endpoint público protegido por `@RateLimited`, recebendo `{ "email": "..." }` e retornando `202 Accepted` sem vazar a existência ou estado da conta.

#### Scenario: Reenvio para conta pendente de verificação

- **WHEN** um cliente anônimo envia `POST /v1/accounts/verifications/request` com um e-mail válido de conta existente não verificada e ativa
- **THEN** o sistema SHALL gerar um novo token, enfileirar o e-mail de verificação e retornar `202 Accepted`.

#### Scenario: Proteção anti-enumeração para e-mail inexistente, indisponível ou já verificado

- **WHEN** um cliente envia `POST /v1/accounts/verifications/request` com um e-mail válido que não existe, pertence a conta indisponível ou pertence a conta já verificada
- **THEN** o sistema SHALL retornar `202 Accepted` sem disparar e-mail de verificação nem revelar o estado da conta.

#### Scenario: Validação de e-mail ausente ou malformado no reenvio

- **WHEN** o payload de `POST /v1/accounts/verifications/request` envia `email` nulo, em branco ou em formato inválido
- **THEN** o sistema SHALL rejeitar a requisição com `400 Bad Request` detalhando o erro no campo `email`.

#### Scenario: Excesso de tentativas de reenvio de verificação

- **WHEN** um cliente ou e-mail excede o limite de requisições da operação `verification` em `POST /v1/accounts/verifications/request`
- **THEN** o sistema SHALL rejeitar a chamada com `429 Too Many Requests`.

### Requirement: Public Atomic Single-Use Email Confirmation

O sistema SHALL expor `POST /v1/accounts/verifications/confirm` como endpoint público protegido por `@RateLimited`, consumindo o token atomicamente em uso único e marcando a conta correspondente como verificada.

#### Scenario: Confirmação bem-sucedida com token válido sem autenticação prévia

- **WHEN** um cliente anônimo envia `POST /v1/accounts/verifications/confirm` com um `token` UUID válido armazenado em `emailVerificationToken`
- **THEN** o sistema SHALL consumir atomicamente o `token` via `TokenManager.consumeToken`, marcar `isVerified = true` na conta associada e retornar `200 OK`.

#### Scenario: Confirmação idempotente quando conta já está verificada

- **WHEN** um token válido é submetido para uma conta cujo campo `isVerified` já é `true`
- **THEN** o sistema SHALL consumir o token e retornar `200 OK` sem falhar.

#### Scenario: Token inexistente, expirado, reutilizado ou consumido concorrentemente

- **WHEN** um cliente envia um `token` UUID inexistente, expirado, já consumido em chamada anterior ou disputado por requisições simultâneas
- **THEN** no máximo uma requisição SHALL retornar `200 OK` e todas as demais SHALL lançar `NotFoundException` retornando `404 Not Found` para o campo `token` com mensagem `"Invalid or expired verification token."`.

#### Scenario: Token ausente ou fora do formato UUID

- **WHEN** `POST /v1/accounts/verifications/confirm` recebe `token` nulo, em branco ou fora do padrão UUID de 36 caracteres
- **THEN** o sistema SHALL rejeitar a requisição com `400 Bad Request` no campo `token`.

#### Scenario: Excesso de tentativas de confirmação de e-mail

- **WHEN** um cliente excede o limite de requisições da operação `verificationConfirm` em `POST /v1/accounts/verifications/confirm`
- **THEN** o sistema SHALL rejeitar a chamada com `429 Too Many Requests`.
