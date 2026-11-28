# Tasks

## 1. Dependência Flyway (Build)

- [x] 1.1 Garantir a declaração `implementation 'org.springframework.boot:spring-boot-starter-flyway'` em `buddy-api/build.gradle` para autoconfiguração automática do Flyway no Spring Boot `4.1.1`.

## 2. Auditoria LGPD Pós-Commit (`domains.audit`)

- [x] 2.1 Alterar `SecurityAuditServiceImpl.logEvent` em `src/main/java/com/buddy/api/domains/audit/services/impl/SecurityAuditServiceImpl.java` para usar `@Transactional(propagation = Propagation.REQUIRES_NEW)`.
- [x] 2.2 Atualizar `src/test/java/com/buddy/api/integrations/web/account/controller/ChangePasswordControllerTest.java` (`integrations/`) para verificar a persistência real de `SecurityEventType.PASSWORD_CHANGED` na tabela `security_audit_event` após `PATCH /v1/accounts/password`.

## 3. Precisão em Milissegundos na Revogação Global (`security.jwt`)

- [x] 3.1 Atualizar `src/main/java/com/buddy/api/commons/configurations/security/jwt/JwtUtil.java` para gravar a claim `iatMs` na geração de access/refresh tokens e expor `getIssuedAtMillisFromToken(String)` com fallback para `iat`.
- [x] 3.2 Atualizar `src/main/java/com/buddy/api/commons/configurations/security/jwt/JwtAuthenticationFilter.java` e `src/main/java/com/buddy/api/domains/authentication/services/impl/AuthServiceImpl.java` para usar `jwtUtil.getIssuedAtMillisFromToken(...)`.
- [x] 3.3 Adicionar testes unitários em `src/test/java/com/buddy/api/units/commons/configurations/security/jwt/JwtUtilTest.java` (`units/`) cobrindo `iatMs` e fallback quando `iatMs` está ausente, além de cenário de integração em `ChangePasswordControllerTest` (`integrations/`) validando novo login imediato após troca de senha.

## 4. Emissão Única de `Set-Cookie` (`security.cookies`)

- [x] 4.1 Refatorar `src/main/java/com/buddy/api/commons/configurations/security/cookies/CookieFactory.java` para emitir um único header `Set-Cookie` por cookie via `jakarta.servlet.http.Cookie` com `setAttribute("SameSite", "Strict")` e apenas `response.addCookie(cookie)`.
- [x] 4.2 Atualizar os testes unitários em `src/test/java/com/buddy/api/units/commons/configurations/secutiry/cookies/CookieFactoryTest.java` (`units/`) validando emissão única via `addCookie` com `SameSite=Strict` e ausência de `addHeader` duplicado.

## 5. Verificação Final

- [x] 5.1 Executar `./gradlew build` em `buddy-api` garantindo sucesso em compilação, testes unitários e de integração, Checkstyle (0 warnings), SpotBugs e PMD.
