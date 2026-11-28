# Design — `public-email-verification-flow`

## Visão Geral

Alinha o ciclo de verificação de e-mail do `buddy-api` ao padrão de mercado para aplicações web abertas (mesmo modelo já utilizado por `ForgotPasswordController` / `ResetPasswordController`), mantendo isolamento DDD dentro do bounded context `account` e proteção por token opaco de uso único, anti-enumeração e rate limiting.

## Decisões Técnicas

### 1. Segurança de Borda (`SecurityConfig`)

- Adicionar `/v1/accounts/verifications/request` e `/v1/accounts/verifications/confirm` à lista `.permitAll()` em `com.buddy.api.commons.configurations.security.SecurityConfig`.

### 2. Consumo Atômico de Token (`TokenManager`)

- Reutilizar exatamente a estratégia de consumo atômico de `ForgotPasswordTokenManager` em `com.buddy.api.commons.configurations.cache.TokenManager`:
  - Injetar `RedisConnectionFactory` (além de `CacheInitializer`).
  - Adicionar método `public String consumeToken(final String token)`:
    - Quando `verificationTokenCache` for `RedisCache`, executar `connection.stringCommands().getDel(key)` com fallback para script Lua `GET_AND_DELETE_SCRIPT` caso o provider Redis lance `UnsupportedOperationException` / `RedisSystemException`, desserializando o e-mail associado.
    - Caso `verificationTokenCache` não seja `RedisCache` (ex.: `ConcurrentMapCache` em testes unitários isolados), ler e remover do `Cache` de forma sincronizada.
    - Quando nenhum e-mail for encontrado (inexistente/expirado/já consumido), lançar `NotFoundException("token", "Invalid or expired verification token.")`.

### 3. Disparo Pós-Commit no Cadastro (`CreateAccountImpl`)

- Em `com.buddy.api.domains.account.services.impl.CreateAccountImpl`:
  - Injetar `ApplicationEventPublisher`, `TokenManager` e `EmailSender` (todos dentro de `domains.account` / `commons`).
  - Após `final var savedAccount = accountRepository.save(accountEntity);`, publicar `eventPublisher.publishEvent(new AccountCreatedEvent(savedAccount.getAccountId(), savedAccount.getEmail()));`.
  - Adicionar listener `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` `handleAccountCreated(final AccountCreatedEvent event)`:
    - Gera o token via `tokenManager.generateAndStoreToken(event.email().value())`.
    - Dispara o e-mail assíncrono via `emailSender.dispatchVerificationEmail(event.accountId(), event.email().value(), token)`.
  - Como `EmailSenderImpl` é `@Async`, falhas de envio após o commit não revertem a criação da conta, e falhas de validação/e-mail duplicado antes do commit não disparam o listener.

### 4. Reenvio Anti-Enumeração e Confirmação Pública (`EmailVerificationServiceImpl`)

- Alterar `EmailVerificationService`:
  - `void requestEmail(EmailAddress email);`
  - `void confirmEmail(String token);`
- Em `EmailVerificationServiceImpl`:
  - Injetar `FindAccount` e `UpdateAccount`, `TokenManager`, `EmailSender`, `AccountValidator`.
  - Em `requestEmail(final EmailAddress email)`:
    - Executar dentro de `try { ... } catch (NotFoundException | AccountUnavailableException | AccountAlreadyVerifiedException ex) { log.debug(...); }`:
      - Buscar conta com `findAccount.findByEmail(email.value())`.
      - Validar `accountValidator.validateAccountNotVerified(account)`.
      - Gerar token e chamar `emailSender.dispatchVerificationEmail(account.accountId(), account.email().value(), token)`.
    - Dessa forma, conta inexistente, conta indisponível ou conta já verificada resultam silenciosamente em `202 Accepted` sem disparo de e-mail nem vazamento de status (anti-enumeração).
  - Em `confirmEmail(final String token)` (`@Transactional`):
    - Consumir atomicamente o token via `final String emailFromToken = tokenManager.consumeToken(token);`.
    - Buscar conta via `final AccountDto account = findAccount.findByEmail(emailFromToken);`.
    - Se `account.isVerified()` for `true`, registrar aviso em log e retornar normalmente (idempotente `200 OK`, token já foi consumido).
    - Caso contrário, chamar `updateAccount.updateIsVerified(account.email().value(), true)`.

### 5. Camada Web (`web.accounts`)

- Criar `RequestEmailVerificationRequest` record puro em `com.buddy.api.web.accounts.requests`:
  - `@NotBlank(message = "Account email is mandatory")`
  - `@Email(message = "Account email must be a valid email address")`
  - `String email`
- Atualizar `EmailVerificationController` e `EmailVerificationControllerDoc`:
  - `POST /v1/accounts/verifications/request` (`202 Accepted`):
    - Recebe `@Valid @RequestBody final RequestEmailVerificationRequest request`.
    - Mantém `@RateLimited(useIp = true, operation = "verification", emailSpel = "#request.email", limitMessage = "Too many verification requests. Please wait a minute before trying again.")`.
    - Chama `emailVerificationService.requestEmail(new EmailAddress(request.email()))`.
  - `POST /v1/accounts/verifications/confirm` (`200 OK`):
    - Recebe `@Valid @RequestBody final ConfirmEmailRequest request`.
    - Proteger com `@RateLimited(useIp = true, operation = "verificationConfirm", limitMessage = "Too many verification confirmation attempts. Please wait a minute before trying again.")`.
    - Chama `emailVerificationService.confirmEmail(request.token())`.

## Conformidade com Qualidade e Testes

- Atualizar `CreateAccountTest`, `EmailVerificationServiceImplTest`, `CreateAccountControllerTest`, `EmailVerificationControllerTest` e adicionar testes unitários de `TokenManager` e `RequestEmailVerificationRequest`.
- Verificar disparo WireMock em `CreateAccountControllerTest` usando Awaitility (`waitUntilWireMockReceives`) após cadastro.
- Validar Checkstyle (0 warnings), SpotBugs e PMD com `./gradlew build`.
