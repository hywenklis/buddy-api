# Proposal

## Why

Os testes end-to-end executados em ambiente Docker local com PostgreSQL 15, Redis e `buddy-client` revelaram quatro falhas reais de confiabilidade e segurança na `buddy-api`:

1. O evento `SecurityEventType.PASSWORD_CHANGED` não está sendo persistido na tabela `security_audit_event` após `PATCH /v1/accounts/password`, pois `SecurityAuditServiceImpl.logEvent` usa `@Transactional` (`Propagation.REQUIRED`) dentro de um listener `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`.
2. A revogação global de tokens (`TokenBlocklistService.revokeAllUserTokens`) compara milissegundos (`Instant.now().toEpochMilli()`) contra a claim `iat` do JWT (precisão de segundos), fazendo com que qualquer novo login dentro do mesmo segundo após reset ou troca de senha tenha seu JWT recém-emitido rejeitado com `403 Forbidden`.
3. `CookieFactory` invoca tanto `response.addCookie(cookie)` quanto `response.addHeader("Set-Cookie", ...)`, emitindo dois cabeçalhos `Set-Cookie` por token (o primeiro sem `SameSite=Strict`).
4. O `build.gradle` com Spring Boot `4.1.1` declarava apenas `flyway-core`, sem `spring-boot-starter-flyway`, impedindo a execução automática das migrações Flyway na inicialização.

## What Changes

- Garantir que `SecurityAuditServiceImpl.logEvent` execute em uma nova transação (`Propagation.REQUIRES_NEW`) para persistir efetivamente eventos `SecurityAuditEventEntity` após o commit da transação principal.
- Adicionar a claim `iatMs` (epoch em milissegundos) na emissão de access e refresh tokens em `JwtUtil`, com leitura retrocompatível em `JwtAuthenticationFilter` e `AuthServiceImpl` (fallback para `iat` em milissegundos), eliminando falsos positivos de revogação no mesmo segundo.
- Remover a emissão duplicada em `CookieFactory`, utilizando `jakarta.servlet.http.Cookie` com `setAttribute("SameSite", "Strict")` e apenas `response.addCookie(cookie)`, emitindo exatamente um header `Set-Cookie` por cookie (`access_token` e `refresh_token`) com `HttpOnly`, `Secure`, `Path=/` e `SameSite=Strict`.
- Formalizar a dependência `org.springframework.boot:spring-boot-starter-flyway` em `build.gradle` para habilitar a autoconfiguração do Flyway no Spring Boot `4.1.1`.

## Capabilities

### New Capabilities
- `authentication/session-security`: Precisão em milissegundos na revogação global de sessões JWT, emissão única e estrita de cookies de autenticação `Set-Cookie` e bootstrap automático de migrações Flyway no Spring Boot 4.

### Modified Capabilities
- `account/change-password`: Exigir persistência transacional independente (`REQUIRES_NEW`) do evento `SecurityEventType.PASSWORD_CHANGED` na fase `TransactionPhase.AFTER_COMMIT`.

## Impact

- **Camadas / Bounded Contexts impactados**: `domains.audit` (`SecurityAuditServiceImpl`), `domains.authentication` (`AuthServiceImpl`), `commons.configurations.security.jwt` (`JwtUtil`, `JwtAuthenticationFilter`), `commons.configurations.security.cookies` (`CookieFactory`), `build.gradle` e suítes `units/` e `integrations/`.
- **APIs / Contratos HTTP**: Nenhuma rota ou schema JSON de requisição/resposta é alterado; os headers `Set-Cookie` passam a ser emitidos sem duplicação e a auditoria LGPD passa a persistir corretamente em banco.

## Fora de Escopo (Out of Scope)

- Alterações no fluxo público ou autenticado de verificação de e-mail (`/v1/accounts/verifications/*`), tratadas em change separada (`public-email-verification-flow`).
- Criação de seeder inicial de `terms_version` em banco limpo (mantido vazio intencionalmente para permitir testes do fluxo de cadastro de termos via API).
- Alterações no frontend `buddy-client`.
