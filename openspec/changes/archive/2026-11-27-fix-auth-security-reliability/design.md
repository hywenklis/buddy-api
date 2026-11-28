# Design — `fix-auth-security-reliability`

## Visão Geral

Esta change resolve quatro falhas pontuais de confiabilidade identificadas em execução real contra PostgreSQL 15, Redis e navegador/HTTP client, preservando todas as fronteiras DDD e contratos públicos de API.

## Decisões Técnicas

### 1. Persistência de Auditoria em `AFTER_COMMIT` (`domains.audit`)

- **Causa raiz**: `ChangePasswordServiceImpl.handlePasswordChanged` usa `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`. Quando o listener é executado após o commit da transação externa de alteração de senha, a sincronização de transação permanece vinculada à thread no Spring; por isso, `@Transactional` com propagação padrão (`Propagation.REQUIRED`) em `SecurityAuditServiceImpl.logEvent` não abre uma nova transação física e o Hibernate não executa flush de `SecurityAuditEventEntity`.
- **Solução**: Alterar `@Transactional` em `SecurityAuditServiceImpl.logEvent` para `@Transactional(propagation = Propagation.REQUIRES_NEW)`.
- **Verificação em integração**: Atualizar `ChangePasswordControllerTest` para consultar `SecurityAuditEventRepository` após `PATCH /v1/accounts/password` e confirmar que existe registro `SecurityEventType.PASSWORD_CHANGED` persistido no PostgreSQL para a conta.

### 2. Precisão em Milissegundos para Revogação Global (`JwtUtil`, `JwtAuthenticationFilter`, `AuthServiceImpl`)

- **Causa raiz**: A especificação JWT RFC 7519 serializa `iat` como inteiro em segundos (`NumericDate`), truncando milissegundos para `000`. Como `TokenBlocklistService.revokeAllUserTokens` armazena `Instant.now().toEpochMilli()`, um token emitido 200 ms após um reset/troca de senha dentro do mesmo segundo possui `issuedAt.toEpochMilli()` menor que `revokedTimestamp`, sendo rejeitado como revogado.
- **Solução**:
  - Em `JwtUtil`:
    - Definir constante `private static final String ISSUED_AT_MS_CLAIM = "iatMs";`.
    - Nos métodos `generateAccessToken` e `generateRefreshToken`, incluir `.claim(ISSUED_AT_MS_CLAIM, now.toEpochMilli())` usando o mesmo `Instant now`.
    - Expor `public long getIssuedAtMillisFromToken(final String token)`: lê `claims.get(ISSUED_AT_MS_CLAIM, Number.class)`; se presente, retorna `number.longValue()`; caso contrário, faz fallback para `getIssuedAtFromToken(token).toEpochMilli()` (validando presença de `iat` quando `iatMs` também está ausente).
  - Em `JwtAuthenticationFilter` e `AuthServiceImpl.refreshToken`:
    - Substituir `jwtUtil.getIssuedAtFromToken(...).toEpochMilli()` por `jwtUtil.getIssuedAtMillisFromToken(...)`.
  - Manter `getIssuedAtFromToken(String)` intacto para compatibilidade com assinaturas e testes existentes.

### 3. Emissão Única de `Set-Cookie` via Servlet 6 `Cookie.setAttribute` (`CookieFactory`)

- **Causa raiz**: `CookieFactory.addCookie` executava `response.addCookie(cookie)` e em seguida `response.addHeader("Set-Cookie", ...)`, gerando cabeçalhos duplicados por cookie (sendo o primeiro gerado pelo Servlet `Cookie` sem `SameSite=Strict`).
- **Solução**:
  - Como o Spring Boot `4.1.1` utiliza Tomcat 11 (Servlet 6.1), `jakarta.servlet.http.Cookie` suporta nativamente `cookie.setAttribute("SameSite", "Strict")`.
  - Em `CookieFactory`, configurar `cookie.setHttpOnly(true)`, `cookie.setSecure(true)`, `cookie.setPath("/")`, `cookie.setMaxAge(maxAge)` e `cookie.setAttribute("SameSite", "Strict")` (com valor `value != null ? value : ""`) e invocar exclusivamente `response.addCookie(cookie)`, removendo `response.addHeader("Set-Cookie", ...)`.
  - Isso garante emissão única de `Set-Cookie` em runtime e mantém total compatibilidade com os matchers `MockMvcResultMatchers.cookie()` e `MockHttpServletResponse.getCookie(...)` em `AuthControllerTest`.
  - Atualizar `CookieFactoryTest` para validar exatamente 2 chamadas a `response.addCookie(...)` com `SameSite=Strict` e `never().addHeader(...)`.

### 4. Bootstrap do Flyway no Spring Boot 4 (`build.gradle`)

- No Spring Boot `4.1.1`, a autoconfiguração do Flyway reside no módulo `org.springframework.boot:spring-boot-starter-flyway`.
- Garantir `implementation 'org.springframework.boot:spring-boot-starter-flyway'` em `buddy-api/build.gradle` mantendo `org.flywaydb:flyway-core` e `org.flywaydb:flyway-database-postgresql`.

## Conformidade com Qualidade e Arquitetura

- Respeitar Checkstyle 13.3.0 (`maxWarnings = 0`, 100 colunas, parâmetros `final`, ordem de imports `STATIC` / `THIRD_PARTY`, sem wildcard imports), SpotBugs (`Effort.MAX`, `Confidence.HIGH`) e PMD 7.22.0.
- Manter testes sem comentários internos, utilizando AssertJ e `@DisplayName`.
