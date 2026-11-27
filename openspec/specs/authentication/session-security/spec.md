# Authentication Session Security Specification

## Purpose

Definir os requisitos de segurança de sessão JWT, emissão única de cookies de autenticação e inicialização automática de migrações Flyway no Spring Boot 4 para o `buddy-api`.

## Requirements

### Requirement: Millisecond Precision on Global Token Revocation

O sistema SHALL registrar e comparar a data de emissão de access e refresh tokens com precisão de milissegundos (`iatMs`) na verificação de revogação global por usuário, mantendo fallback para a claim `iat` padrão quando `iatMs` estiver ausente.

#### Scenario: Novo login no mesmo segundo após revogação global é aceito

- **WHEN** um usuário tem todas as sessões revogadas via `TokenBlocklistService.revokeAllUserTokens` e realiza novo login ou refresh dentro do mesmo segundo civil
- **THEN** o token recém-emitido com `iatMs` posterior ao timestamp de revogação SHALL ser aceito normalmente nas requisições autenticadas e no fluxo de refresh.

#### Scenario: Token emitido antes da revogação no mesmo segundo é rejeitado

- **WHEN** um token foi emitido com `iatMs` anterior ao timestamp registrado em `jwt:revoke_all:<email>` dentro do mesmo segundo civil
- **THEN** `JwtAuthenticationFilter` e `AuthServiceImpl.refreshToken` SHALL considerar o token revogado e rejeitar o seu uso.

#### Scenario: Token legado sem claim `iatMs` usa fallback de `iat`

- **WHEN** um token válido não possui a claim `iatMs`
- **THEN** o sistema SHALL utilizar `claims.getIssuedAt().toInstant().toEpochMilli()` para validar revogação global sem lançar exceção.

### Requirement: Single Strict Set-Cookie Header per Auth Cookie

O sistema SHALL emitir exatamente um header `Set-Cookie` para `access_token` e um header `Set-Cookie` para `refresh_token`, com os atributos `Path=/`, `HttpOnly`, `Secure` e `SameSite=Strict`, tanto na emissão quanto na invalidação.

#### Scenario: Emissão de cookies no login sem cabeçalhos duplicados

- **WHEN** `CookieFactory.addAuthCookies` grava os cookies de autenticação na resposta HTTP
- **THEN** a resposta SHALL conter exatamente 2 headers `Set-Cookie` (um para `access_token` e um para `refresh_token`), ambos com `Path=/`, `HttpOnly`, `Secure`, `SameSite=Strict` e seus respectivos valores de `Max-Age`.

#### Scenario: Invalidação de cookies no logout ou alteração de senha

- **WHEN** `CookieFactory.invalidateAuthCookies` limpa os cookies de autenticação na resposta HTTP
- **THEN** a resposta SHALL conter exatamente 2 headers `Set-Cookie` com valor vazio, `Max-Age=0`, `Path=/`, `HttpOnly`, `Secure` e `SameSite=Strict`.

### Requirement: Automatic Flyway Migration Bootstrap on Spring Boot 4

O projeto `buddy-api` SHALL incluir o starter oficial do Flyway para Spring Boot 4 para garantir que as migrações SQL sejam executadas automaticamente na inicialização da aplicação.

#### Scenario: Inicialização da aplicação contra banco PostgreSQL vazio

- **WHEN** a aplicação sobe com Spring Boot `4.1.1` conectada a um banco PostgreSQL 15 sem tabelas criadas
- **THEN** o Flyway SHALL executar automaticamente todas as migrações versionadas antes do processamento de requisições HTTP.
