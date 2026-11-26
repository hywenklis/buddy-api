# Proposal

## Why

A Milestone 5 (`feat/m5-pets-v2`, PR #382) implementou a API REST `/v2/pets`, os serviços de domínio V2, as JPA Specifications modulares e a migração de backfill antes da adoção oficial do fluxo OpenSpec no repositório. Esta change formaliza retroativamente a especificação canônica da capability e consolida os ajustes identificados na auditoria do código, na revisão automatizada (CodeRabbit) e nos guardrails arquiteturais do projeto antes do merge em `develop`.

## What Changes

- Formalização da nova API REST `/v2/pets` (`POST /v2/pets/register`, `GET /v2/pets`, `GET /v2/pets/{id}`, `PUT /v2/pets/{id}`) com paginação imutável `PageResponse<T>` sem HATEOAS e endpoints públicos de leitura (`GET /v2/pets/**`).
- Formalização das regras de domínio V2 (`CreatePetV2`, `FindPetV2`, `GetPetV2`, `UpdatePetV2`), filtros dinâmicos modulares (`PetV2Specifications`), carregamento em lote de imagens ordenadas por `display_order` e proteção Anti-IDOR com suporte a `ROLE_ADMIN`.
- Formalização do isolamento estrito entre bounded contexts via serviços (`FindAccount.findActiveById`, `FindProfile` com value object `EmailAddress`, `FindImage`) e correção do mapeamento `BYTEA` (`@JdbcTypeCode(SqlTypes.VARBINARY)`) em `ImageEntity`.
- Formalização da migration Flyway idempotente `V20260827_4__backfill_pet_to_pet_v2.sql` para migração de registros legados de `pet` e `shelter` para `pet_v2` e `image`.
- Correções planejadas para conformidade total com a especificação e os guardrails do projeto:
  - Seleção determinística exclusiva de perfil ativo `SHELTER` (`profile_type = 'SHELTER'` + `DISTINCT ON (p.id)`) no backfill SQL `V20260827_4__backfill_pet_to_pet_v2.sql`, mitigando risco de vínculo indevido em contas multi-perfil.
  - Ajuste de `@PreAuthorize` em `PetV2Controller.updatePet` para `(hasRole('SHELTER') or hasRole('ADMIN')) and hasAuthority('SCOPE_VERIFIED')`, alinhando o contrato de segurança ao bypass de `ROLE_ADMIN` já existente em `UpdatePetV2Impl`.
  - Alinhamento da documentação OpenAPI de `isForAdoption` em `PetV2SearchCriteriaRequest` e `PetV2ControllerDoc` (parâmetro opcional; quando omitido, retorna pets disponíveis e indisponíveis).
  - Reforço de validação na borda em `UpdatePetV2Request.name` (`@Pattern(regexp = ".*\\S.*")`) e `@Digits(integer = 8, fraction = 2)` em `size` e `weight` de `CreatePetV2Request` e `UpdatePetV2Request`.
  - Adição de `@RateLimited(useIp = true, operation = "updatePetV2", ...)` em `PUT /v2/pets/{id}` com resposta `429` documentada no OpenAPI.
  - Adequação de localização e higiene dos testes (`PetV2DomainMapperTest` em `mappers/v2/`, `PetV2SpecificationsTest` em `specifications/v2/`, remoção de comentários em testes) e ampliação dos cenários de integração em `PetV2ControllerTest` e `BackfillPetV2MigrationTest`.

## Capabilities

### New Capabilities

- `pet/pets-v2`: Gestão e consulta pública de pets V2 (`/v2/pets`), incluindo cadastro autenticado por abrigo, busca paginada com filtros dinâmicos, consulta detalhada com imagens ordenadas, atualização parcial com proteção Anti-IDOR e bypass administrativo, rate limiting nos endpoints mutantes, isolamento inter-domínio e backfill idempotente de dados legados.

### Modified Capabilities

<!-- Nenhuma capability existente em openspec/specs/ sofre alteração de requisitos. -->

## Impact

- **Bounded contexts e camadas impactados**:
  - `com.buddy.api.domains.pet` (DTOs v2, `PetV2DomainMapper`, `PetV2Repository`, serviços v2 `CreatePetV2`/`FindPetV2`/`GetPetV2`/`UpdatePetV2`, `PetV2Specifications` modulares).
  - `com.buddy.api.domains.account` (`FindAccount.findActiveById`).
  - `com.buddy.api.domains.profile` (`FindProfile`, `ProfileRepository`).
  - `com.buddy.api.domains.image` (`ImageEntity`, `ImageRepository`, `FindImage`).
  - `com.buddy.api.domains.authentication` (`AuthenticatedUser.isAdmin`, `AuthServiceImpl`, `CustomUserDetailsService`).
  - `com.buddy.api.commons` (`SecurityConfig`, `PageResponse`, `PetNotFoundException`, `ActiveShelterProfileNotFoundException`).
  - `com.buddy.api.web.pets.v2` (`PetV2Controller`, `PetV2ControllerDoc`, requests, responses e mappers MapStruct).
  - `src/main/resources/db/migration` (`V20260827_4__backfill_pet_to_pet_v2.sql`).
  - `src/test/java/com/buddy/api/units/` e `src/test/java/com/buddy/api/integrations/` (cobertura unitária, integração MockMvc/Testcontainers e testes de migration).

## Fora de Escopo (Out of Scope)

- Remoção, depreciação ou alteração comportamental dos endpoints legados `/v1/pets`.
- Upload, atualização, reordenação ou exclusão de imagens de pets V2 via API REST.
- Fluxos de solicitação, avaliação ou acompanhamento de adoção V2 (`domains.adoption`).
- Endpoints de exclusão lógica ou física (`DELETE /v2/pets/{id}`).
