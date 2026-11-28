# Design

## Context

A Milestone 5 (`feat/m5-pets-v2`) introduz a segunda versão da API de pets (`/v2/pets`) sobre a modelagem `pet_v2` e `image` criada anteriormente nas migrações `V20241014_1__create_table_pet_v2.sql` e `V20241014_2__create_table_image.sql`. A base de código da branch já possui a maior parte da implementação e 550 testes aprovados, porém uma auditoria técnica identificou lacunas entre a implementação, a revisão automatizada (PR #382 / CodeRabbit) e os guardrails arquiteturais do projeto (veja `proposal.md` para motivação e escopo).

## Goals / Non-Goals

**Goals:**
- Consolidar o contrato arquitetural de `/v2/pets` respeitando estritamente o isolamento DDD entre `pet`, `account`, `profile`, `image` e `authentication`.
- Garantir paginação enxuta via `PageResponse<T>` imutável, sem HATEOAS, e carregamento de imagens livre de N+1.
- Corrigir a autorização administrativa em `PUT /v2/pets/{id}`, a proteção contra abuso via `@RateLimited`, a consistência OpenAPI de `isForAdoption` e as validações de borda alinhadas ao schema SQL (`DECIMAL(10, 2)`).
- Eliminar o risco de atribuição incorreta de ownership no backfill legado `V20260827_4__backfill_pet_to_pet_v2.sql`.
- Alinhar a estrutura e higiene dos testes unitários e de integração às convenções do repositório.

**Non-Goals:**
- Alterar ou remover endpoints V1 (`/v1/pets`).
- Criar novos endpoints para upload, ordenação ou exclusão de imagens em `image`.
- Implementar exclusão de pets (`DELETE /v2/pets/{id}`) ou fluxos de adoção V2 (`domains.adoption`).

## Decisions

### 1. Arquitetura em camadas e fronteiras DDD
- **Decisão**: O fluxo segue estritamente `PetV2Controller` → `PetV2RequestMapper` / `PetV2ResponseMapper` (MapStruct) → Serviços `CreatePetV2`, `FindPetV2`, `GetPetV2`, `UpdatePetV2` → `PetV2Repository` e interfaces inter-domínio (`FindAccount`, `FindProfile`, `FindImage`).
- **Racional**: Evita acoplamento direto de controllers e serviços de `pet` a `AccountRepository`, `ProfileRepository` ou `ImageRepository`. `FindProfile.findByAccountEmail` adota o value object `EmailAddress` para eliminar strings brutas na camada de domínio.
- **Alternativa considerada**: Resolver `ProfileEntity` no controller (implementação inicial da PR #382), descartada por violar a fronteira DDD de camada web.

### 2. Paginação imutável `PageResponse<T>` e carregamento anti-N+1 de imagens
- **Decisão**: Utilizar o record genérico `com.buddy.api.commons.page.PageResponse<T>` com cópia defensiva `List.copyOf` no construtor compacto e accessor `content()`. Em `FindPetV2Impl`, quando a página não está vazia, buscar todas as imagens em uma única query via `FindImage.findByPetV2_PetV2IdInOrderByDisplayOrderAsc(petIds)` e agrupar em memória por `petV2Id`.
- **Racional**: Elimina o overhead de serialização HATEOAS (`PagedModel`) da V1 e evita N+1 queries na renderização de `coverImageUrl` nas listagens. Quando `page.isEmpty()` é verdadeiro, a chamada a `FindImage` é ignorada.
- **Alternativa considerada**: `@EntityGraph` ou `JOIN FETCH` diretamente sobre `PetV2Entity` com coleção bidirecional de `ImageEntity`, descartado para manter `PetV2Entity` desacoplada da tabela `image` (que possui relação polimórfica XOR entre `profile_id` e `pet_v2_id`).

### 3. Specifications dinâmicas modulares e contrato de `isForAdoption`
- **Decisão**: Decompor `PetV2Specifications` em quatro utilitários coesos sob `com.buddy.api.domains.pet.specifications.v2`:
  - `PetV2BasicSpecifications` (`species`, `gender`, `isNeutered`, `isForAdoption`);
  - `PetV2TextSpecifications` (`nameContains` com `trim()` e `Locale.ROOT`);
  - `PetV2MetricSpecifications` (`sizeBetween`, `weightBetween`, `ageBetween`);
  - `PetV2RelationshipSpecifications` (`hasGuardianProfileId`).
- **Decisão de contrato para `isForAdoption`**: Quando omitido (`null`) em `GET /v2/pets`, `PetV2BasicSpecifications.isForAdoption(null)` retorna `cb.conjunction()` (sem filtro), permitindo listar todos os pets de um abrigo em uma única consulta. A descrição `@Parameter` de `PetV2SearchCriteriaRequest.isForAdoption` e o resumo em `PetV2ControllerDoc.findPets` serão ajustados para refletir o comportamento opcional sem mencionar `(default: true)`.
- **Alternativa considerada**: Forçar `isForAdoption = true` como default quando `null`; descartada porque impediria consumidores (como o painel do abrigo) de consultar pets disponíveis e indisponíveis em uma única requisição.

### 4. Autorização Anti-IDOR e acesso administrativo em `PUT /v2/pets/{id}`
- **Decisão**: Manter a regra de ownership e bypass administrativo em `UpdatePetV2Impl` (`findAccount.findActiveById` obrigatório para qualquer ator; se `!isAdmin`, exige `findActiveShelterProfileByAccountId` -> `422` `ActiveShelterProfileNotFoundException` e valida `guardianProfile.profileId` -> `403` `UnauthorizedEntityAccessException`) e corrigir `@PreAuthorize` em `PetV2Controller.updatePet` para:
  `(hasRole('SHELTER') or hasRole('ADMIN')) and hasAuthority('SCOPE_VERIFIED')`.
- **Racional**: Na branch atual, `@PreAuthorize("hasRole('SHELTER') and hasAuthority('SCOPE_VERIFIED')")` bloqueava contas `ROLE_ADMIN` antes mesmo de alcançar `UpdatePetV2Impl`, tornando o parâmetro `AuthenticatedUser.isAdmin()` morto na camada HTTP.

### 5. Validações de borda e Rate Limiting nos endpoints mutantes
- **Decisão**:
  - Em `UpdatePetV2Request.name`, adicionar `@Pattern(regexp = ".*\\S.*", message = "Pet name cannot be blank")` para impedir nomes compostos exclusivamente por espaços mantendo a opcionalidade do campo no `PUT` parcial.
  - Em `CreatePetV2Request` e `UpdatePetV2Request`, adicionar `@Digits(integer = 8, fraction = 2)` em `size` e `weight` para alinhar a validação HTTP às colunas `DECIMAL(10, 2)` da tabela `pet_v2`.
  - Em `PetV2Controller.updatePet`, adicionar `@RateLimited(useIp = true, operation = "updatePetV2", limitMessage = "Too many pet update requests. Please wait a minute before trying again.")` e declarar `@ApiResponse(responseCode = "429", ...)` em `PetV2ControllerDoc.updatePet`, alinhando a proteção à já aplicada em `POST /v2/pets/register` (`operation = "createPetV2"`).
- **Racional**: Evita persistência de strings em branco em updates, evita truncamento silencioso ou erro de escala no PostgreSQL e protege ambas as operações de escrita contra abuso.

### 6. Backfill SQL determinístico e seguro para autorização
- **Decisão**: Atualizar `src/main/resources/db/migration/V20260827_4__backfill_pet_to_pet_v2.sql` para selecionar o perfil guardião via `SELECT DISTINCT ON (p.id)` com filtro explícito `pr.is_deleted = FALSE AND pr.profile_type = 'SHELTER'` e ordenação `ORDER BY p.id, pr.creation_date ASC, pr.profile_id ASC`.
- **Racional**: Como `feat/m5-pets-v2` ainda não foi mergeada em `develop` nem aplicada em produção, ajustar diretamente a migration `V20260827_4__backfill_pet_to_pet_v2.sql` mantém o histórico Flyway limpo e elimina o risco de vincular pets legados a perfis `USER` ou `ADMIN` (o que quebraria o `PUT /v2/pets/{id}` para o abrigo legítimo).
- **Alternativa considerada**: Criar uma segunda migration corretiva após `V20260827_4`, descartada por desnecessária antes do merge da feature branch.

### 7. Mapeamento binário de `ImageEntity` e higiene de testes
- **Decisão**: Manter `@JdbcTypeCode(SqlTypes.VARBINARY)` em `ImageEntity.imageData` para compatibilidade estrita com `BYTEA` no PostgreSQL 15. Reorganizar `PetV2DomainMapperTest` para `units/domains/pet/mappers/v2/` e `PetV2SpecificationsTest` para `units/domains/pet/specifications/v2/`, remover comentários em arquivos de teste e cobrir todos os cenários HTTP (`200`, `201`, `400`, `401`/`403`, `404`, `422`, `429`) e de backfill multi-perfil.

## Risks / Trade-offs

- **[Risco] Contas legadas em `shelter` sem perfil `SHELTER` ativo não serão migradas pelo backfill** → **Mitigação**: Comportamento intencional e seguro. Vincular um pet a um perfil `USER` ou `ADMIN` geraria falha `422`/`403` no `PUT /v2/pets/{id}` e violaria a semântica do domínio; eventuais abrigos legados sem perfil `SHELTER` devem ter o perfil criado antes da reexecução idempotente ou migração dedicada.
- **[Risco] Mudança do `@PreAuthorize` em `PUT /v2/pets/{id}` amplia acesso para `ROLE_ADMIN`** → **Mitigação**: Exige simultaneamente `SCOPE_VERIFIED` e conta ativa validada via `FindAccount.findActiveById` (rejeitando contas deletadas, bloqueadas ou não verificadas), além de cobertura explícita em `PetV2ControllerTest`.
- **[Risco] Comportamento de `isForAdoption` omitido retornar também pets indisponíveis exige que o frontend da vitrine pública envie `?isForAdoption=true`** → **Mitigação**: Documentado explicitamente no Swagger (`PetV2ControllerDoc` e `PetV2SearchCriteriaRequest`) e na especificação canônica `pet/pets-v2`.
