# Tasks

## 1. Domínio `profile` — Consulta Determinística Multi-Perfil

- [x] 1.1 Substituir `Optional<ProfileEntity> findByAccount_AccountIdAndProfileTypeAndIsDeletedFalse(UUID, ProfileTypeEnum)` em `src/main/java/com/buddy/api/domains/profile/repositories/ProfileRepository.java` por `@Query` explícita `List<ProfileEntity> findActiveByAccountIdAndTypeOrdered(UUID accountId, ProfileTypeEnum profileType)` ordenada por `creationDate ASC, profileId ASC`.
- [x] 1.2 Adicionar `findActiveProfilesByAccountIdAndType(UUID accountId, ProfileTypeEnum profileType)` e `findActiveShelterProfilesByAccountId(UUID accountId)` retornando `List<ProfileEntity>` em `src/main/java/com/buddy/api/domains/profile/services/FindProfile.java` e `src/main/java/com/buddy/api/domains/profile/services/impl/FindProfileImpl.java`, fazendo `findActiveProfileByAccountIdAndType` e `findActiveShelterProfileByAccountId` delegarem via `.stream().findFirst()` (seleção determinística sem `IncorrectResultSizeDataAccessException`).
- [x] 1.3 Atualizar e expandir testes unitários em `src/test/java/com/buddy/api/units/domains/profile/services/FindProfileImplTest.java` cobrindo lista com múltiplos perfis `SHELTER` ativos ordenados, retorno vazio para entradas nulas e seleção determinística do primeiro perfil em `findActiveShelterProfileByAccountId`.

## 2. Domínio `pet` — Ownership Multi-`SHELTER` e Cadastro Determinístico

- [x] 2.1 Atualizar `src/main/java/com/buddy/api/domains/pet/services/v2/impl/UpdatePetV2Impl.java` para buscar todos os perfis `SHELTER` ativos via `findProfile.findActiveShelterProfilesByAccountId(authenticatedAccountId)`, lançar `ActiveShelterProfileNotFoundException` (`422`) quando a lista estiver vazia e validar `validateOwnership(pet.getGuardianProfile(), activeShelterProfiles)` comparando `guardianProfile.getProfileId()` contra todos os perfis ativos e tratando `guardian == null` ou `guardian.getProfileId() == null` com `UnauthorizedEntityAccessException` (`403`).
- [x] 2.2 Atualizar e expandir testes unitários em `src/test/java/com/buddy/api/units/domains/pet/services/v2/UpdatePetV2ImplTest.java` cobrindo conta com múltiplos perfis `SHELTER` ativos atualizando pet vinculado ao segundo perfil (`200`), conta com múltiplos perfis `SHELTER` ativos tentando atualizar pet de terceiro (`403`), pet com `guardianProfile` nulo (`403`) e lista vazia de perfis `SHELTER` ativos (`422`).
- [x] 2.3 Verificar e manter testes unitários em `src/test/java/com/buddy/api/units/domains/pet/services/v2/CreatePetV2ImplTest.java` alinhados à seleção determinística em `findProfile.findActiveShelterProfileByAccountId(accountId)`.

## 3. Testes Unitários de Bean Validation (`ValidatorFactory` Lifecycle)

- [x] 3.1 Atualizar `src/test/java/com/buddy/api/units/web/pets/v2/requests/CreatePetV2RequestTest.java`, `src/test/java/com/buddy/api/units/web/pets/v2/requests/UpdatePetV2RequestTest.java` e `src/test/java/com/buddy/api/units/web/pets/v2/requests/PetV2SearchCriteriaRequestTest.java` para manter `private static ValidatorFactory validatorFactory;` aberta durante a execução dos testes (`@BeforeAll` inicializa `validatorFactory` e `validator`; `@AfterAll` fecha `validatorFactory` com verificação null-safe).

## 4. Testes de Integração (`PetV2ControllerTest`)

- [x] 4.1 Anotar `src/test/java/com/buddy/api/integrations/web/pets/v2/controllers/PetV2ControllerTest.java` com `@Execution(ExecutionMode.SAME_THREAD)` preservando intactas as asserções de `429 Too Many Requests` na 6ª requisição em `POST /v2/pets/register` e `PUT /v2/pets/{id}`.
- [x] 4.2 Substituir as chamadas diretas a `createPetV2.create` e `updatePetV2.update` nos testes de `422` em `src/test/java/com/buddy/api/integrations/web/pets/v2/controllers/PetV2ControllerTest.java` por testes via `MockMvc` (`POST /v2/pets/register` e `PUT /v2/pets/{id}`) via `MockMvcBuilders.webAppContextSetup(webApplicationContext).build()` com `SecurityContextHolder` populado com `AuthenticatedUser` (`ROLE_SHELTER` + `SCOPE_VERIFIED`) em bloco `try/finally` (`SecurityContextHolder.clearContext()`), validando HTTP `422` e `$.errors[0].field == "profile"` através de `@PreAuthorize`, controller e `GlobalExceptionHandler`.
- [x] 4.3 Adicionar testes de integração via `MockMvc` em `src/test/java/com/buddy/api/integrations/web/pets/v2/controllers/PetV2ControllerTest.java` cobrindo: token JWT real de conta cujo perfil `SHELTER` possui `isDeleted = true` retornando `403 Forbidden` em `POST` e `PUT`; conta com dois perfis `SHELTER` ativos cadastrando pet vinculado ao perfil mais antigo por `creation_date ASC, profile_id ASC` (`201`) e atualizando pet pertencente ao segundo perfil `SHELTER` ativo (`200`), sem comentários dentro do código de teste.

## 5. Quality Gate e Verificação Final

- [x] 5.1 Executar `./gradlew build` em `buddy-api` garantindo compilação limpa, aprovação de todos os testes unitários (`units/`) e de integração (`integrations/`), Checkstyle com `maxWarnings = 0`, SpotBugs/FindSecBugs e PMD 7.22.0 sem violações.
- [x] 5.2 Executar `openspec validate "pets-v2-pr-review-fixes" --strict` e verificar conformidade entre implementação e especificação (`openspec-verify-change`) antes de arquivar a change.
