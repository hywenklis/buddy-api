# Design

## Context

Na implementação atual da branch `feat/m5-pets-v2` (PR #382), a tabela `profile` (`V20241011_2__create_table_profile.sql`) e `CreateProfileImpl` permitem que uma mesma `AccountEntity` possua múltiplos perfis `SHELTER` ativos (`is_deleted = FALSE`). Contudo, `ProfileRepository.findByAccount_AccountIdAndProfileTypeAndIsDeletedFalse` retorna `Optional<ProfileEntity>` sem ordenação ou limite, fazendo com que `FindProfileImpl.findActiveShelterProfileByAccountId` lance `IncorrectResultSizeDataAccessException` (HTTP 500) tanto em `UpdatePetV2Impl.update` quanto em `CreatePetV2Impl.create` quando há 2 ou mais perfis `SHELTER` ativos.

Além disso, a auditoria dos testes e da cadeia de filtros de segurança (`JwtAuthenticationFilter` + `CustomUserDetailsService` + `GlobalExceptionHandler`) revelou três melhorias de testabilidade e conformidade:
1. Em `PetV2ControllerTest`, os testes de perfil `SHELTER` ausente chamavam `createPetV2.create` e `updatePetV2.update` diretamente, sem atravessar `MockMvc`, `@PreAuthorize` e `GlobalExceptionHandler`; e com JWT real sem perfil ativo, `CustomUserDetailsService` omite `ROLE_SHELTER`, gerando `403` antes de alcançar o controller.
2. `IntegrationTestAbstract` já aplica `@Isolated`, mas `PetV2ControllerTest` se beneficia de `@Execution(ExecutionMode.SAME_THREAD)` explícito para tornar determinística a execução sequencial dos testes de rate limiting (`429`) no Redis/Bucket4j.
3. `CreatePetV2RequestTest`, `UpdatePetV2RequestTest` e `PetV2SearchCriteriaRequestTest` fecham a `ValidatorFactory` em `@BeforeAll` com `try-with-resources`, violando o contrato Jakarta Validation antes dos métodos `@Test`.

## Goals / Non-Goals

**Goals:**
- Eliminar `IncorrectResultSizeDataAccessException` em contas com múltiplos perfis `SHELTER` ativos nas operações `POST /v2/pets/register` e `PUT /v2/pets/{id}`, mantendo o isolamento DDD via `FindProfile`.
- Tratar explicitamente `guardianProfile == null` na validação de ownership em `UpdatePetV2Impl`.
- Testar o fluxo HTTP completo de `422 Unprocessable Entity` via `MockMvc` em `PetV2ControllerTest` (e documentar/testar o comportamento `403 Forbidden` via JWT real quando o perfil `SHELTER` foi deletado).
- Blindar `PetV2ControllerTest` contra interferência de concorrência nos testes de rate limit (`429`) e corrigir o ciclo de vida de `ValidatorFactory` nos três testes unitários de request V2.

**Non-Goals:**
- Alterar o schema da tabela `profile` ou criar migration Flyway impondo unicidade por `(account_id, profile_type)`.
- Alterar `CreateProfileImpl` para bloquear criação de múltiplos perfis `SHELTER`.
- Alterar o contrato REST de `POST /v2/pets/register` para receber `guardianProfileId`.
- Modificar `openspec/changes/archive/2026-11-26-pets-v2-api/tasks.md` ou `BuddySecurityPropertiesTest`.

## Decisions

### 1. Consulta determinística de perfis `SHELTER` ativos em `ProfileRepository` e `FindProfile`
- **Decisão**:
  - Substituir `Optional<ProfileEntity> findByAccount_AccountIdAndProfileTypeAndIsDeletedFalse(UUID accountId, ProfileTypeEnum profileType)` em `ProfileRepository` por uma consulta JPQL explícita `@Query`:
    `List<ProfileEntity> findActiveByAccountIdAndTypeOrdered(@Param("accountId") UUID accountId, @Param("profileType") ProfileTypeEnum profileType)`
    filtrando `p.account.accountId = :accountId AND p.profileType = :profileType AND p.isDeleted = false` com `ORDER BY p.creationDate ASC, p.profileId ASC`.
  - Em `FindProfile` e `FindProfileImpl`:
    - Expor `List<ProfileEntity> findActiveProfilesByAccountIdAndType(UUID accountId, ProfileTypeEnum profileType)` e `List<ProfileEntity> findActiveShelterProfilesByAccountId(UUID accountId)`, retornando `List.of()` quando `accountId == null` ou `profileType == null`.
    - Manter `Optional<ProfileEntity> findActiveProfileByAccountIdAndType(UUID accountId, ProfileTypeEnum profileType)` e `Optional<ProfileEntity> findActiveShelterProfileByAccountId(UUID accountId)` retornando `findActiveProfilesByAccountIdAndType(accountId, profileType).stream().findFirst()`.
- **Racional**:
  - Garante que `CreatePetV2Impl` selecione deterministicamente o perfil `SHELTER` ativo mais antigo (`creation_date ASC, profile_id ASC`), alinhado à ordenação já adotada em `V20260827_4__backfill_pet_to_pet_v2.sql` e validada em `BackfillPetV2MigrationTest`, mantendo nomes de método curtos compatíveis com o limite de 100 colunas do Checkstyle.
  - Permite que `UpdatePetV2Impl` receba todos os perfis `SHELTER` ativos da conta sem consultar `ProfileRepository` diretamente, preservando o isolamento inter-domínio.
- **Alternativa considerada**: Restringir `profile` a no máximo 1 perfil `SHELTER` ativo por conta via unique index parcial no PostgreSQL e validação em `CreateProfileImpl`. Descartada por ampliar o escopo para o domínio `profile`, exigir migration sobre dados potencialmente existentes e conflitar com o cenário multi-`SHELTER` do backfill.

### 2. Verificação de ownership multi-`SHELTER` e guardião nulo em `UpdatePetV2Impl`
- **Decisão**: Em `UpdatePetV2Impl.update`, quando `!isAdmin`:
  1. Consultar `final var activeShelterProfiles = findProfile.findActiveShelterProfilesByAccountId(authenticatedAccountId);`.
  2. Se `activeShelterProfiles.isEmpty()`, lançar `ActiveShelterProfileNotFoundException(authenticatedAccountId)` (`422`).
  3. Validar ownership via `validateOwnership(pet.getGuardianProfile(), activeShelterProfiles)`: verificar se `guardian != null`, `guardian.getProfileId() != null` e `activeShelterProfiles.stream().anyMatch(profile -> guardian.getProfileId().equals(profile.getProfileId()))`; caso contrário, lançar `UnauthorizedEntityAccessException("Access denied: You do not have permission to update this pet.")` (`403`).
- **Racional**: Garante que uma conta com 2 ou mais perfis `SHELTER` ativos consiga atualizar pets vinculados a qualquer um de seus perfis `SHELTER` ativos, sem quebrar se o pet possuir `guardianProfile` nulo (falha segura com `403` em vez de `NullPointerException`).

### 3. Cobertura HTTP `422` via `MockMvc` respeitando `CustomUserDetailsService`
- **Decisão**:
  - Em `PetV2ControllerTest`, substituir as chamadas diretas a `createPetV2.create(...)` e `updatePetV2.update(...)` nos testes de `422` por requisições `MockMvc` (`POST /v2/pets/register` e `PUT /v2/pets/{id}`), construindo um `MockMvc` via `MockMvcBuilders.webAppContextSetup(webApplicationContext).build()` com `SecurityContextHolder` populado com `AuthenticatedUser` (`ROLE_SHELTER` e `SCOPE_VERIFIED`) para uma `AccountEntity` persistida sem perfil `SHELTER` ativo dentro de bloco `try/finally` (`SecurityContextHolder.clearContext()`).
  - Validar `status().is(422)` e `jsonPath("$.errors[0].field").value("profile")`, exercitando Jackson, `@Valid`, `@PreAuthorize`, `@RateLimited`, `@AuthenticationPrincipal`, `PetV2Controller`, `CreatePetV2Impl`/`UpdatePetV2Impl`, PostgreSQL e `GlobalExceptionHandler`.
  - Adicionar testes `MockMvc` complementares via `this.mockMvc` (cadeia `SecurityFilterChain` completa) usando token JWT real emitido para uma conta cujo perfil `SHELTER` foi deletado (`isDeleted = true`), validando que `CustomUserDetailsService` não emite `ROLE_SHELTER` e os endpoints `POST` e `PUT` retornam `403 Forbidden`.
- **Racional**: `JwtAuthenticationFilter` ignora claims de role do JWT e recarrega authorities do banco via `CustomUserDetailsService`; além disso, `SecurityConfig` utiliza `SessionCreationPolicy.STATELESS` (`NullSecurityContextRepository`), o que faria o `SecurityContextHolderFilter` sobrescrever um `SecurityContext` injetado via `RequestPostProcessor` quando a `SecurityFilterChain` completa é executada sem `spring-security-test`. O uso de `MockMvcBuilders.webAppContextSetup(webApplicationContext).build()` permite simular a condição de corrida pós-autenticação atravessando `@PreAuthorize` AOP, controller, serviços e `GlobalExceptionHandler` sem adicionar `spring-security-test` ao `build.gradle`, mantendo os testes com JWT real na cadeia completa de segurança.

### 4. Isolamento explícito de thread em `PetV2ControllerTest`
- **Decisão**: Anotar `PetV2ControllerTest` com `@Execution(ExecutionMode.SAME_THREAD)` (mantendo a herança de `@Isolated` de `IntegrationTestAbstract`), preservando intactas as asserções de 5 respostas de sucesso seguidas de `429 Too Many Requests` na 6ª requisição em `POST /v2/pets/register` e `PUT /v2/pets/{id}`.
- **Racional**: Embora `@Isolated` já force execução isolada da classe, `@Execution(ExecutionMode.SAME_THREAD)` explícito documenta e garante no nível da classe que nenhum método dentro de `PetV2ControllerTest` executará concorrentemente apesar de `src/test/resources/junit-platform.properties` definir `junit.jupiter.execution.parallel.mode.default = concurrent`, impedindo compartilhamento de chaves Bucket4j por IP ou reset de Redis por `@BeforeEach` paralelo.

### 5. Ciclo de vida de `ValidatorFactory` em testes unitários de Bean Validation
- **Decisão**: Em `CreatePetV2RequestTest`, `UpdatePetV2RequestTest` e `PetV2SearchCriteriaRequestTest`, armazenar `private static ValidatorFactory validatorFactory;` como campo estático inicializado em `@BeforeAll` (obtendo `validator = validatorFactory.getValidator()`) e fechado em `@AfterAll` com checagem `if (validatorFactory != null) { validatorFactory.close(); }`.
- **Racional**: Cumpre estritamente o contrato de `jakarta.validation.ValidatorFactory.close()`, que proíbe o uso de instâncias `Validator` após o fechamento da fábrica, mantendo a liberação do recurso ao fim da suíte de cada classe.

## Risks / Trade-offs

- **[Trade-off] Contas com múltiplos perfis `SHELTER` ativos sempre vinculam novos pets em `POST /v2/pets/register` ao perfil mais antigo** → **Mitigação**: Critério determinístico (`creation_date ASC, profile_id ASC`) idêntico ao da migration de backfill `V20260827_4__backfill_pet_to_pet_v2.sql`, evitando `500` inesperado; caso o produto passe a permitir seleção de perfil no cadastro, bastará adicionar `guardianProfileId` opcional ao contrato sem quebrar o comportamento padrão.
- **[Risco] `SecurityContextHolder` sujo em testes caso uma exceção ocorra antes da limpeza** → **Mitigação**: Envolver a execução ou usar `SecurityContextHolder.clearContext()` via `try/finally` ou `RequestPostProcessor` aliado à limpeza padrão por requisição no filtro, garantindo isolamento entre testes.
