# Tasks

## 1. Baseline Já Implementado na Branch `feat/m5-pets-v2` (Auditoria)

- [x] 1.1 Implementar infraestrutura transversal `PageResponse<T>`, `PetNotFoundException`, `ActiveShelterProfileNotFoundException` e liberação pública `GET /v2/pets/**` em `SecurityConfig`, com testes em `units/commons/page/PageResponseTest.java` e `units/commons/exceptions/PetNotFoundExceptionTest.java`.
- [x] 1.2 Implementar isolamento inter-domínio e mapeamento de imagens: `FindAccount.findActiveById`, `FindProfile` com `EmailAddress`, `FindImage`, `ImageRepository` e `@JdbcTypeCode(SqlTypes.VARBINARY)` em `ImageEntity`, com testes em `units/domains/services/impl/FindAccountTest.java`, `units/domains/profile/services/FindProfileImplTest.java` e `units/domains/image/services/FindImageImplTest.java`.
- [x] 1.3 Implementar camada de domínio Pets V2: DTOs `record` (`CreatePetV2Dto`, `PetV2Dto`, `PetV2ImageDto`, `PetV2SearchCriteriaDto`, `UpdatePetV2Dto`), `PetV2DomainMapper`, `PetV2Repository`, `PetV2Specifications` modulares (`Basic`, `Text`, `Metric`, `Relationship`) e serviços (`CreatePetV2Impl`, `FindPetV2Impl`, `GetPetV2Impl`, `UpdatePetV2Impl`), com testes unitários em `units/domains/pet/` e integração em `integrations/domains/pet/repositories/PetV2RepositoryTest.java`.
- [x] 1.4 Implementar camada web `/v2/pets`: `PetV2Controller`, `PetV2ControllerDoc`, `CreatePetV2Request`, `PetV2SearchCriteriaRequest`, `UpdatePetV2Request`, `PetV2Response`, `PetV2SummaryResponse`, `PetV2ImageResponse`, `PetV2RequestMapper` e `PetV2ResponseMapper`, com testes unitários em `units/web/pets/v2/` e integração base em `integrations/web/pets/v2/controllers/PetV2ControllerTest.java`.
- [x] 1.5 Implementar migration Flyway inicial `src/main/resources/db/migration/V20260827_4__backfill_pet_to_pet_v2.sql` e teste de execução dupla em `integrations/migrations/BackfillPetV2MigrationTest.java`.

## 2. Correções de Segurança, Contrato OpenAPI e Validação de Borda

- [x] 2.1 Atualizar `@PreAuthorize` em `src/main/java/com/buddy/api/web/pets/v2/controllers/PetV2Controller.java` no método `updatePet` para `(hasRole('SHELTER') or hasRole('ADMIN')) and hasAuthority('SCOPE_VERIFIED')`.
- [x] 2.2 Adicionar `@RateLimited(useIp = true, operation = "updatePetV2", limitMessage = "Too many pet update requests. Please wait a minute before trying again.")` em `PetV2Controller.updatePet` e documentar `@ApiResponse(responseCode = "429", ...)` em `src/main/java/com/buddy/api/web/pets/v2/controllers/PetV2ControllerDoc.java`.
- [x] 2.3 Alinhar a documentação OpenAPI de `isForAdoption` em `src/main/java/com/buddy/api/web/pets/v2/requests/PetV2SearchCriteriaRequest.java` (remover `(default: true)`, descrever como filtro opcional) e ajustar a descrição do método `findPets` em `PetV2ControllerDoc.java`.
- [x] 2.4 Adicionar `@Pattern(regexp = ".*\\S.*", message = "Pet name cannot be blank")` em `UpdatePetV2Request.name` e `@Digits(integer = 8, fraction = 2, ...)` nos campos `size` e `weight` de `src/main/java/com/buddy/api/web/pets/v2/requests/CreatePetV2Request.java` e `src/main/java/com/buddy/api/web/pets/v2/requests/UpdatePetV2Request.java`.

## 3. Correção Determinística do Backfill SQL

- [x] 3.1 Atualizar `src/main/resources/db/migration/V20260827_4__backfill_pet_to_pet_v2.sql` para selecionar o perfil guardião com `SELECT DISTINCT ON (p.id)`, filtrando `pr.is_deleted = FALSE AND pr.profile_type = 'SHELTER'` e ordenando por `ORDER BY p.id, pr.creation_date ASC, pr.profile_id ASC`.

## 4. Adequação de Organização e Higiene dos Testes

- [x] 4.1 Mover `src/test/java/com/buddy/api/units/domains/pet/mappers/PetV2DomainMapperTest.java` para `src/test/java/com/buddy/api/units/domains/pet/mappers/v2/PetV2DomainMapperTest.java` ajustando a declaração `package`.
- [x] 4.2 Mover `src/test/java/com/buddy/api/units/domains/pet/specifications/PetV2SpecificationsTest.java` para `src/test/java/com/buddy/api/units/domains/pet/specifications/v2/PetV2SpecificationsTest.java` ajustando a declaração `package`.
- [x] 4.3 Remover todos os comentários em código de teste de `src/test/java/com/buddy/api/integrations/migrations/BackfillPetV2MigrationTest.java`, `src/test/java/com/buddy/api/integrations/domains/pet/repositories/PetV2RepositoryTest.java` e verificar conformidade em todas as suítes V2 (`units/` e `integrations/`).

## 5. Ampliação de Cobertura de Testes Unitários e de Integração

- [x] 5.1 Adicionar testes unitários de Bean Validation em `src/test/java/com/buddy/api/units/web/pets/v2/requests/CreatePetV2RequestTest.java` e `src/test/java/com/buddy/api/units/web/pets/v2/requests/UpdatePetV2RequestTest.java` cobrindo `name` somente com espaços, limites de `approximateAge`, `description`, default `isForAdoption` e `@Digits(integer = 8, fraction = 2)` em `size`/`weight`.
- [x] 5.2 Expandir `src/test/java/com/buddy/api/integrations/web/pets/v2/controllers/PetV2ControllerTest.java` (estendendo `IntegrationTestAbstract`) para cobrir `PUT /v2/pets/{id}` por `ROLE_ADMIN` sem perfil `SHELTER` (`200`), `PUT` com payload inválido (`400`), `PUT` sem token (`403`), `PUT` para pet inexistente (`404`), `PUT` por `ROLE_SHELTER` sem perfil ativo (`422`), `POST` sem `ROLE_SHELTER` ou sem `SCOPE_VERIFIED` (`403`), `POST` sem perfil `SHELTER` ativo (`422`), `GET /v2/pets` com `isForAdoption` omitido retornando `true` e `false`, e rate limiting (`429`) em `POST` e `PUT`.
- [x] 5.3 Expandir `src/test/java/com/buddy/api/integrations/migrations/BackfillPetV2MigrationTest.java` para validar conta multi-perfil (`USER` + `SHELTER` e múltiplos `SHELTER`) vinculando deterministicamente ao perfil `SHELTER` esperado e conta sem perfil `SHELTER` ativo não vinculando pet a perfil `USER`.

## 6. Quality Gate e Verificação Final

- [x] 6.1 Executar `./gradlew build` garantindo aprovação de todos os testes unitários e de integração, Checkstyle com `maxWarnings = 0`, SpotBugs/FindSecBugs e PMD 7.22.0 sem violações.
- [x] 6.2 Executar verificação de conformidade entre implementação e especificação (`openspec-verify-change`) antes do arquivamento (`openspec-archive-change`).
