# Tasks

## 1. Camada Cache e Segurança (`commons.configurations`)

- [x] 1.1 Atualizar `src/main/java/com/buddy/api/commons/configurations/security/SecurityConfig.java` para incluir `/v1/accounts/verifications/request` e `/v1/accounts/verifications/confirm` em `.permitAll()`.
- [x] 1.2 Atualizar `src/main/java/com/buddy/api/commons/configurations/cache/TokenManager.java` adicionando `consumeToken(String token)` com consumo atômico no Redis (`getDel` + fallback Lua script) e `NotFoundException` quando inexistente/expirado.
- [x] 1.3 Criar testes unitários para `TokenManager` em `src/test/java/com/buddy/api/units/commons/configurations/cache/TokenManagerTest.java` (`units/`).

## 2. Domínio de Conta e Verificação (`domains.account`)

- [x] 2.1 Atualizar `src/main/java/com/buddy/api/domains/account/services/impl/CreateAccountImpl.java` para publicar `AccountCreatedEvent` e processar em `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` gerando token e disparando e-mail de verificação.
- [x] 2.2 Atualizar `src/main/java/com/buddy/api/domains/account/email/services/EmailVerificationService.java` e `EmailVerificationServiceImpl.java` para reenvio anti-enumeração por `EmailAddress` e confirmação pública por `token` consumido atomicamente.
- [x] 2.3 Atualizar os testes unitários em `src/test/java/com/buddy/api/units/domains/services/impl/CreateAccountTest.java` e `src/test/java/com/buddy/api/units/domains/services/impl/EmailVerificationServiceImplTest.java` (`units/`).

## 3. Camada Web (`web.accounts`)

- [x] 3.1 Criar `src/main/java/com/buddy/api/web/accounts/requests/RequestEmailVerificationRequest.java` com validação `@NotBlank` e `@Email`.
- [x] 3.2 Atualizar `src/main/java/com/buddy/api/web/accounts/controllers/EmailVerificationController.java` e `EmailVerificationControllerDoc.java` removendo `@AuthenticationPrincipal`, usando `RequestEmailVerificationRequest` em `/request` e adicionando `@RateLimited` em `/confirm`.
- [x] 3.3 Atualizar os testes de integração em `src/test/java/com/buddy/api/integrations/web/account/controller/CreateAccountControllerTest.java` e `src/test/java/com/buddy/api/integrations/web/email/controller/EmailVerificationControllerTest.java` (`integrations/`) cobrindo disparo automático pós-cadastro, acesso anônimo, anti-enumeração, uso único/concorrência (`404` na reutilização), validação (`400`) e rate limiting (`429`).

## 4. Verificação Final

- [x] 4.1 Executar `./gradlew build` em `buddy-api` garantindo compilação, testes unitários e de integração, Checkstyle (0 warnings), SpotBugs e PMD.
