# Pets V2 Specification

## Purpose

Definir o contrato da API REST `/v2/pets` para cadastro, busca pública paginada com filtros dinâmicos, consulta detalhada com imagens ordenadas, atualização parcial com proteção Anti-IDOR e migração idempotente de dados legados do domínio de pets.

## ADDED Requirements

### Requirement: Pet V2 Registration

O sistema SHALL permitir que usuários autenticados com perfil ativo de abrigo (`ROLE_SHELTER` e `SCOPE_VERIFIED`) cadastrem novos pets via `POST /v2/pets/register`, vinculando o registro ao seu perfil `SHELTER` ativo e retornando `201 Created`.

#### Scenario: Cadastro completo de pet por abrigo autenticado e verificado

- **WHEN** um usuário com `ROLE_SHELTER`, `SCOPE_VERIFIED`, conta ativa e perfil `SHELTER` não deletado envia `POST /v2/pets/register` com `name`, `species`, `gender`, `approximateAge`, `size`, `weight`, `isNeutered`, `isForAdoption` e `description` válidos
- **THEN** o sistema SHALL persistir o registro em `pet_v2` associado ao `profile_id` do abrigo autenticado, definir `ageReportDate` com a data atual quando `approximateAge` for informado e retornar `201 Created` com `CreatedSuccessResponse`.

#### Scenario: Cadastro omitindo isForAdoption e approximateAge

- **WHEN** o cliente envia `POST /v2/pets/register` com os campos obrigatórios válidos omitindo `isForAdoption` e `approximateAge`
- **THEN** o sistema SHALL assumir `isForAdoption = true` por padrão, persistir `approximateAge = null` e `ageReportDate = null` e retornar `201 Created`.

#### Scenario: Falha de validação de borda no cadastro

- **WHEN** o payload de `POST /v2/pets/register` possui `name` em branco ou fora de 2 a 100 caracteres, `species` ou `gender` nulo, `approximateAge` fora de 0 a 50, `size` ou `weight` não positivo ou com mais de 8 dígitos inteiros ou 2 casas decimais, ou `description` acima de 1000 caracteres
- **THEN** o sistema SHALL rejeitar a requisição com `400 Bad Request` contendo `ErrorResponse` com o detalhamento dos campos inválidos.

#### Scenario: Requisição de cadastro sem autenticação ou com credenciais inválidas

- **WHEN** um cliente envia `POST /v2/pets/register` sem token de autenticação válido
- **THEN** a camada de segurança SHALL bloquear a requisição antes de executar o controller retornando `403 Forbidden` (ausência de token) ou `401 Unauthorized` (credenciais inválidas).

#### Scenario: Requisição de cadastro sem role SHELTER ou sem verificação

- **WHEN** um usuário autenticado sem `ROLE_SHELTER` ou sem `SCOPE_VERIFIED` envia `POST /v2/pets/register`
- **THEN** o sistema SHALL bloquear o acesso retornando `403 Forbidden`.

#### Scenario: Conta autenticada deletada, bloqueada ou não verificada no cadastro

- **WHEN** a conta autenticada localizada por `FindAccount.findActiveById` está deletada, bloqueada ou não verificada
- **THEN** o sistema SHALL rejeitar a operação com `AccountUnavailableException`, `AccountBlockedException` ou `AccountNotVerifiedException` sem persistir o pet.

#### Scenario: Abrigo sem perfil SHELTER ativo no cadastro

- **WHEN** um usuário com `ROLE_SHELTER` e `SCOPE_VERIFIED` não possui perfil ativo do tipo `SHELTER` (`isDeleted = false`)
- **THEN** o sistema SHALL lançar `ActiveShelterProfileNotFoundException` retornando `422 Unprocessable Entity` para o campo `profile`.

### Requirement: Public Paginated Pet V2 Search

O sistema SHALL expor busca paginada pública via `GET /v2/pets` retornando `PageResponse` sem HATEOAS, com filtros dinâmicos opcionais, ordenação padrão por `creationDate DESC` e carregamento em lote da imagem de capa ordenada.

#### Scenario: Busca pública paginada com múltiplos filtros sem autenticação

- **WHEN** um cliente anônimo envia `GET /v2/pets` com combinações válidas de `name` (parcial case-insensitive com `trim()` e `Locale.ROOT`), `species`, `gender`, `isNeutered`, `isForAdoption`, `guardianProfileId`, `minSize`/`maxSize`, `minWeight`/`maxWeight`, `minAge`/`maxAge` e paginação
- **THEN** o sistema SHALL retornar `200 OK` com `PageResponse<PetV2SummaryResponse>` contendo os pets correspondentes, metadados (`page`, `size`, `totalElements`, `totalPages`, `isFirst`, `isLast`, `hasNext`) sem `_links` HATEOAS, ordenados por `creationDate DESC` quando `sort` não for informado, e `coverImageUrl` selecionada pela imagem de menor `displayOrder`.

#### Scenario: Busca pública omitindo o parâmetro isForAdoption

- **WHEN** um cliente envia `GET /v2/pets` sem informar `isForAdoption`
- **THEN** o sistema SHALL não aplicar filtro sobre `isForAdoption`, retornando tanto pets com `isForAdoption = true` quanto `isForAdoption = false`.

#### Scenario: Busca pública filtrando explicitamente por disponibilidade de adoção

- **WHEN** um cliente envia `GET /v2/pets?isForAdoption=true` ou `GET /v2/pets?isForAdoption=false`
- **THEN** o sistema SHALL retornar apenas os pets cujo campo `isForAdoption` corresponde ao booleano informado.

#### Scenario: Busca pública sem resultados

- **WHEN** nenhum pet corresponde aos critérios informados em `GET /v2/pets`
- **THEN** o sistema SHALL retornar `200 OK` com `content` vazio, `totalElements = 0` e sem executar consulta adicional de imagens em lote.

#### Scenario: Critérios de busca com limites negativos ou intervalos invertidos

- **WHEN** um cliente envia `GET /v2/pets` com `minSize`, `maxSize`, `minWeight`, `maxWeight`, `minAge` ou `maxAge` negativo, ou com `minSize > maxSize`, `minWeight > maxWeight` ou `minAge > maxAge`
- **THEN** o sistema SHALL rejeitar a requisição com `400 Bad Request` detalhando o erro de validação.

### Requirement: Public Pet V2 Details

O sistema SHALL expor consulta pública detalhada de um pet via `GET /v2/pets/{id}`, retornando `200 OK` com dados completos, imagem de capa e lista de imagens ordenadas por `displayOrder` ascendente.

#### Scenario: Consulta detalhada de pet existente com imagens

- **WHEN** um cliente anônimo envia `GET /v2/pets/{id}` para um pet existente com imagens associadas
- **THEN** o sistema SHALL retornar `200 OK` com `PetV2Response` contendo `id`, `guardianProfileId`, `name`, `species`, `gender`, `approximateAge`, `ageReportDate`, `size`, `weight`, `isNeutered`, `isForAdoption`, `description`, `coverImageUrl` (menor `displayOrder` com `nullsLast`), `images` ordenadas por `displayOrder ASC`, `creationDate` e `updatedDate`.

#### Scenario: Consulta detalhada de pet existente sem imagens

- **WHEN** um cliente envia `GET /v2/pets/{id}` para um pet existente sem imagens cadastradas
- **THEN** o sistema SHALL retornar `200 OK` com `coverImageUrl = null` e `images = []`.

#### Scenario: Consulta detalhada de pet inexistente

- **WHEN** um cliente envia `GET /v2/pets/{id}` com um UUID que não existe em `pet_v2`
- **THEN** o sistema SHALL lançar `PetNotFoundException` retornando `404 Not Found` para o campo `petId`.

#### Scenario: Consulta detalhada com identificador UUID inválido

- **WHEN** um cliente envia `GET /v2/pets/{id}` com um valor que não é um UUID válido
- **THEN** o sistema SHALL retornar `400 Bad Request` para o parâmetro `id`.

### Requirement: Pet V2 Partial Update and Anti-IDOR Authorization

O sistema SHALL permitir atualização parcial via `PUT /v2/pets/{id}` apenas para `ROLE_SHELTER` proprietário do pet ou `ROLE_ADMIN` com conta ativa e verificada (`SCOPE_VERIFIED`), aplicando proteção Anti-IDOR e retornando `200 OK`.

#### Scenario: Atualização parcial bem-sucedida pelo abrigo proprietário

- **WHEN** o usuário autenticado (`ROLE_SHELTER` + `SCOPE_VERIFIED`) cujo perfil `SHELTER` ativo é o `guardianProfile` do pet envia `PUT /v2/pets/{id}` com um subconjunto de campos válidos
- **THEN** o sistema SHALL atualizar apenas os campos não nulos enviados, atualizar `ageReportDate = LocalDate.now()` caso `approximateAge` seja informado e retornar `200 OK` com `PetV2Response` atualizado e suas imagens ordenadas.

#### Scenario: Atualização bem-sucedida por conta administrativa sem perfil SHELTER

- **WHEN** um usuário autenticado com `ROLE_ADMIN`, `SCOPE_VERIFIED` e conta ativa (ainda que sem perfil `SHELTER`) envia `PUT /v2/pets/{id}` com payload válido para um pet existente
- **THEN** o sistema SHALL validar a conta ativa via `FindAccount.findActiveById`, ignorar a restrição de ownership de abrigo e retornar `200 OK` com o pet atualizado.

#### Scenario: Tentativa de atualização por outro abrigo (Anti-IDOR)

- **WHEN** um usuário com `ROLE_SHELTER` e `SCOPE_VERIFIED` tenta atualizar via `PUT /v2/pets/{id}` um pet pertencente a outro `guardianProfile`
- **THEN** o sistema SHALL lançar `UnauthorizedEntityAccessException` retornando `403 Forbidden` sem modificar o registro.

#### Scenario: Atualização sem autenticação ou sem role autorizada

- **WHEN** uma requisição `PUT /v2/pets/{id}` é enviada sem token, com credenciais inválidas ou por usuário sem `ROLE_SHELTER`/`ROLE_ADMIN` ou sem `SCOPE_VERIFIED`
- **THEN** a camada de segurança SHALL bloquear a requisição com `403 Forbidden` (sem token ou sem permissão) ou `401 Unauthorized` (credenciais inválidas).

#### Scenario: Atualização de pet inexistente

- **WHEN** um usuário autorizado envia `PUT /v2/pets/{id}` para um `id` inexistente em `pet_v2`
- **THEN** o sistema SHALL lançar `PetNotFoundException` retornando `404 Not Found` para o campo `petId`.

#### Scenario: Atualização por abrigo sem perfil SHELTER ativo

- **WHEN** um usuário não administrador com `ROLE_SHELTER` e `SCOPE_VERIFIED` não possui perfil `SHELTER` ativo e envia `PUT /v2/pets/{id}` para um pet existente
- **THEN** o sistema SHALL lançar `ActiveShelterProfileNotFoundException` retornando `422 Unprocessable Entity` para o campo `profile`.

#### Scenario: Falha de validação de borda na atualização

- **WHEN** o payload de `PUT /v2/pets/{id}` envia `name` composto apenas por espaços em branco ou fora de 2 a 100 caracteres, `approximateAge` fora de 0 a 50, `size` ou `weight` não positivo ou excedendo `DECIMAL(10, 2)`, ou `description` acima de 1000 caracteres
- **THEN** o sistema SHALL rejeitar a requisição com `400 Bad Request` detalhando os campos inválidos.

### Requirement: Pet V2 Mutating Endpoints Rate Limiting

O sistema SHALL proteger os endpoints mutantes `POST /v2/pets/register` e `PUT /v2/pets/{id}` com rate limiting baseado em IP e operação, retornando `429 Too Many Requests` quando o limite configurado for excedido.

#### Scenario: Excesso de requisições no cadastro de pet V2

- **WHEN** um cliente excede o limite de requisições configurado para a operação `createPetV2` em `POST /v2/pets/register`
- **THEN** o sistema SHALL bloquear a chamada com `429 Too Many Requests` e a mensagem configurada na anotação `@RateLimited`.

#### Scenario: Excesso de requisições na atualização de pet V2

- **WHEN** um cliente excede o limite de requisições configurado para a operação `updatePetV2` em `PUT /v2/pets/{id}`
- **THEN** o sistema SHALL bloquear a chamada com `429 Too Many Requests` e a mensagem configurada na anotação `@RateLimited`.

### Requirement: Cross-Domain Boundary Isolation and Image Mapping

O sistema SHALL isolar os bounded contexts sem acesso direto a repositórios de outros domínios, usando value object `EmailAddress` na busca de perfis por e-mail e mapeamento binário `BYTEA` compatível com PostgreSQL para imagens.

#### Scenario: Isolamento de acesso entre controller e domínios

- **WHEN** o fluxo de pets V2 resolve conta ativa, perfil `SHELTER` ativo ou imagens associadas aos pets
- **THEN** o controller SHALL delegar exclusivamente aos serviços de domínio de `pet` e os serviços de `pet` SHALL comunicar com `account`, `profile` e `image` apenas pelas interfaces `FindAccount`, `FindProfile` e `FindImage`.

#### Scenario: Busca segura de imagens para entradas nulas ou vazias

- **WHEN** `FindImage` é invocado com entidade `PetV2Entity` nula ou lista de UUIDs nula ou vazia
- **THEN** o serviço SHALL retornar lista vazia sem consultar o banco de dados.

#### Scenario: Persistência de imagem compatível com BYTEA

- **WHEN** uma `ImageEntity` com ou sem `imageData` binário é persistida e consultada no PostgreSQL
- **THEN** o mapeamento `@JdbcTypeCode(SqlTypes.VARBINARY)` SHALL serializar e desserializar a coluna `image_data` (`BYTEA`) sem erro de tipo JDBC/OID.

### Requirement: Idempotent Deterministic Legacy Pet Backfill Migration

O sistema SHALL prover migração Flyway idempotente de backfill dos registros legados (`pet` e `shelter`) para `pet_v2` e `image`, selecionando de forma determinística exclusivamente um perfil ativo do tipo `SHELTER`.

#### Scenario: Migração inicial de pets legados e avatares

- **WHEN** a migração `V20260827_4__backfill_pet_to_pet_v2.sql` é executada sobre registros legados em `pet` vinculados a `shelter`, `account` e perfil ativo `SHELTER`
- **THEN** o sistema SHALL inserir os pets em `pet_v2` preservando `pet.id` como `pet_v2_id`, normalizando `specie` e `gender`, calculando `approximate_age` e `age_report_date` a partir de `birth_date`, definindo `is_for_adoption = TRUE` e migrando `pet.avatar` não vazio para `image` com `is_avatar = TRUE`, `image_status = 'APPROVED'` e `display_order = 0`.

#### Scenario: Reexecução idempotente da migração de backfill

- **WHEN** a migração `V20260827_4__backfill_pet_to_pet_v2.sql` é executada múltiplas vezes sobre a mesma base
- **THEN** o sistema SHALL não duplicar registros em `pet_v2` nem em `image`.

#### Scenario: Seleção determinística em conta com múltiplos perfis ativos

- **WHEN** uma conta vinculada a um registro legado em `shelter` possui múltiplos perfis ativos (por exemplo `USER` e `SHELTER`, ou mais de um `SHELTER`)
- **THEN** a migração SHALL vincular o pet exclusivamente a um perfil com `is_deleted = FALSE` e `profile_type = 'SHELTER'`, selecionando deterministicamente o primeiro por `creation_date ASC, profile_id ASC`.

#### Scenario: Conta sem perfil SHELTER ativo durante o backfill

- **WHEN** uma conta vinculada a `shelter` possui apenas perfis `USER`/`ADMIN` ativos ou perfil `SHELTER` com `is_deleted = TRUE`
- **THEN** a migração SHALL não vincular o pet legado a perfis `USER` ou `ADMIN`.
