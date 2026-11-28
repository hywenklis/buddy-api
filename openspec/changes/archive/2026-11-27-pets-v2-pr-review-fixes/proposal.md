# Proposal

## Why

A revisão automatizada do CodeRabbit no PR #382 (`feat/m5-pets-v2`) apontou uma falha de integridade (`IncorrectResultSizeDataAccessException` / HTTP 500) em contas com múltiplos perfis `SHELTER` ativos e lacunas de confiabilidade nos testes da API `/v2/pets`. Além disso, a auditoria do código identificou que `CreatePetV2Impl` compartilha a mesma consulta frágil e que `PetV2SearchCriteriaRequestTest` possui o mesmo fechamento prematuro de `ValidatorFactory`, exigindo correção coordenada antes do merge em `develop`.

## What Changes

- Substituir a busca frágil de perfil único (`Optional<ProfileEntity>`) em `ProfileRepository` e `FindProfile` por consulta determinística ordenada (`creation_date ASC, profile_id ASC`) que suporte tanto a listagem completa (`findActiveShelterProfilesByAccountId`) quanto a seleção determinística do primeiro perfil ativo (`findActiveShelterProfileByAccountId`), eliminando `IncorrectResultSizeDataAccessException` (HTTP 500) em contas com 2 ou mais perfis `SHELTER` ativos.
- Atualizar `UpdatePetV2Impl.update` para validar ownership contra todos os perfis `SHELTER` ativos da conta autenticada: lista vazia lança `ActiveShelterProfileNotFoundException` (`422`), e `guardianProfile` nulo ou não presente na lista lança `UnauthorizedEntityAccessException` (`403`).
- Garantir que `CreatePetV2Impl.create` utilize a seleção determinística (`creation_date ASC, profile_id ASC`) em contas com múltiplos perfis `SHELTER` ativos, mantendo consistência com o backfill SQL `V20260827_4__backfill_pet_to_pet_v2.sql`.
- Expandir `PetV2ControllerTest` para cobrir HTTP `422` via `MockMvc` em `POST /v2/pets/register` e `PUT /v2/pets/{id}` atravessando `@PreAuthorize`, `PetV2Controller`, serviços e `GlobalExceptionHandler` (injetando `AuthenticatedUser` com `ROLE_SHELTER` e `SCOPE_VERIFIED` via `RequestPostProcessor` para simular remoção de perfil pós-autenticação), adicionar teste com JWT real para conta com perfil `SHELTER` deletado retornando `403 Forbidden`, cobrir contas com múltiplos perfis `SHELTER` ativos em `POST` e `PUT`, e anotar `PetV2ControllerTest` com `@Execution(ExecutionMode.SAME_THREAD)` para isolar os testes de rate limiting (`429`).
- Corrigir o ciclo de vida de `ValidatorFactory` (`private static ValidatorFactory`, aberto em `@BeforeAll` e fechado em `@AfterAll`) em `CreatePetV2RequestTest`, `UpdatePetV2RequestTest` e `PetV2SearchCriteriaRequestTest`.

## Capabilities

### New Capabilities

<!-- Nenhuma nova capability é introduzida por esta change. -->

### Modified Capabilities

- `pet/pets-v2`: Atualização dos requisitos de cadastro (`Pet V2 Registration`) e atualização parcial (`Pet V2 Partial Update and Anti-IDOR Authorization`) para suportar contas com múltiplos perfis `SHELTER` ativos de forma determinística, tratar `guardianProfile` nulo na verificação de ownership e formalizar o comportamento `403 Forbidden` via JWT quando o perfil `SHELTER` estiver deletado.

## Impact

- **Bounded contexts e camadas impactados**:
  - `com.buddy.api.domains.profile` (`ProfileRepository`, `FindProfile`, `FindProfileImpl`).
  - `com.buddy.api.domains.pet` (`UpdatePetV2Impl`, `CreatePetV2Impl`).
  - `src/test/java/com/buddy/api/units/` (`FindProfileImplTest`, `UpdatePetV2ImplTest`, `CreatePetV2ImplTest`, `CreatePetV2RequestTest`, `UpdatePetV2RequestTest`, `PetV2SearchCriteriaRequestTest`).
  - `src/test/java/com/buddy/api/integrations/` (`PetV2ControllerTest`).

## Fora de Escopo (Out of Scope)

- Alteração do schema da tabela `profile` ou criação de nova migration Flyway para restringir contas a um único perfil `SHELTER` ativo.
- Alteração do contrato REST de `POST /v2/pets/register` para aceitar `guardianProfileId` explícito no payload.
- Edição do artefato já arquivado `openspec/changes/archive/2026-11-26-pets-v2-api/tasks.md` (alertas de gramática do LanguageTool sobre backticks são falsos positivos).
- Alteração de `BuddySecurityPropertiesTest` ou de outros domínios fora de `pet`/`profile`.
