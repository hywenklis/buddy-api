## MODIFIED Requirements

### Requirement: Pet V2 Registration

O sistema SHALL permitir que usuários autenticados com perfil ativo de abrigo (`ROLE_SHELTER` e `SCOPE_VERIFIED`) cadastrem novos pets via `POST /v2/pets/register`, vinculando o registro ao seu perfil `SHELTER` ativo (selecionando deterministicamente o perfil ativo mais antigo por `creation_date ASC, profile_id ASC` quando a conta possuir múltiplos perfis `SHELTER` ativos) e retornando `201 Created`.

#### Scenario: Cadastro completo de pet por abrigo autenticado e verificado

- **WHEN** um usuário com `ROLE_SHELTER`, `SCOPE_VERIFIED`, conta ativa e perfil `SHELTER` não deletado envia `POST /v2/pets/register` com `name`, `species`, `gender`, `approximateAge`, `size`, `weight`, `isNeutered`, `isForAdoption` e `description` válidos
- **THEN** o sistema SHALL persistir o registro em `pet_v2` associado ao `profile_id` do abrigo autenticado, definir `ageReportDate` com a data atual quando `approximateAge` for informado e retornar `201 Created` com `CreatedSuccessResponse`.

#### Scenario: Cadastro por conta com múltiplos perfis SHELTER ativos

- **WHEN** um usuário autenticado com `ROLE_SHELTER`, `SCOPE_VERIFIED` e conta ativa possui dois ou mais perfis `SHELTER` com `isDeleted = false` e envia `POST /v2/pets/register` com payload válido
- **THEN** o sistema SHALL selecionar deterministicamente o perfil `SHELTER` ativo mais antigo ordenado por `creation_date ASC, profile_id ASC`, sem lançar erro de multiplicidade, persistir o pet vinculado a esse perfil e retornar `201 Created`.

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

#### Scenario: Requisição de cadastro com JWT de conta cujo perfil SHELTER foi deletado

- **WHEN** um cliente envia `POST /v2/pets/register` com token JWT válido para uma conta ativa e verificada cujo único perfil `SHELTER` possui `isDeleted = true`
- **THEN** a camada de autenticação SHALL derivar as permissões a partir dos perfis ativos no banco sem conceder `ROLE_SHELTER` e bloquear a requisição com `403 Forbidden`.

#### Scenario: Conta autenticada deletada, bloqueada ou não verificada no cadastro

- **WHEN** a conta autenticada localizada por `FindAccount.findActiveById` está deletada, bloqueada ou não verificada
- **THEN** o sistema SHALL rejeitar a operação com `AccountUnavailableException`, `AccountBlockedException` ou `AccountNotVerifiedException` sem persistir o pet.

#### Scenario: Abrigo sem perfil SHELTER ativo no cadastro

- **WHEN** um usuário com `ROLE_SHELTER` e `SCOPE_VERIFIED` não possui perfil ativo do tipo `SHELTER` (`isDeleted = false`)
- **THEN** o sistema SHALL lançar `ActiveShelterProfileNotFoundException` retornando `422 Unprocessable Entity` para o campo `profile`.

### Requirement: Pet V2 Partial Update and Anti-IDOR Authorization

O sistema SHALL permitir atualização parcial via `PUT /v2/pets/{id}` apenas para `ROLE_SHELTER` proprietário do pet (validando o `guardianProfile` contra todos os perfis `SHELTER` ativos da conta autenticada) ou `ROLE_ADMIN` com conta ativa e verificada (`SCOPE_VERIFIED`), aplicando proteção Anti-IDOR e retornando `200 OK`.

#### Scenario: Atualização parcial bem-sucedida pelo abrigo proprietário

- **WHEN** o usuário autenticado (`ROLE_SHELTER` + `SCOPE_VERIFIED`) cujo perfil `SHELTER` ativo é o `guardianProfile` do pet envia `PUT /v2/pets/{id}` com um subconjunto de campos válidos
- **THEN** o sistema SHALL atualizar apenas os campos não nulos enviados, atualizar `ageReportDate = LocalDate.now()` caso `approximateAge` seja informado e retornar `200 OK` com `PetV2Response` atualizado e suas imagens ordenadas.

#### Scenario: Atualização por conta com múltiplos perfis SHELTER ativos

- **WHEN** um usuário não administrador com `ROLE_SHELTER` e `SCOPE_VERIFIED` possui dois ou mais perfis `SHELTER` ativos (`isDeleted = false`) e envia `PUT /v2/pets/{id}` para um pet cujo `guardianProfile` pertence a qualquer um desses perfis ativos
- **THEN** o sistema SHALL validar a propriedade comparando `guardianProfile` com todos os perfis `SHELTER` ativos da conta autenticada, sem lançar erro de multiplicidade, e retornar `200 OK` com o pet atualizado.

#### Scenario: Atualização bem-sucedida por conta administrativa sem perfil SHELTER

- **WHEN** um usuário autenticado com `ROLE_ADMIN`, `SCOPE_VERIFIED` e conta ativa (ainda que sem perfil `SHELTER`) envia `PUT /v2/pets/{id}` com payload válido para um pet existente
- **THEN** o sistema SHALL validar a conta ativa via `FindAccount.findActiveById`, ignorar a restrição de ownership de abrigo e retornar `200 OK` com o pet atualizado.

#### Scenario: Tentativa de atualização por outro abrigo (Anti-IDOR)

- **WHEN** um usuário com `ROLE_SHELTER` e `SCOPE_VERIFIED` tenta atualizar via `PUT /v2/pets/{id}` um pet pertencente a outro `guardianProfile`
- **THEN** o sistema SHALL lançar `UnauthorizedEntityAccessException` retornando `403 Forbidden` sem modificar o registro.

#### Scenario: Tentativa de atualização de pet com guardianProfile nulo por abrigo

- **WHEN** um usuário não administrador com `ROLE_SHELTER`, `SCOPE_VERIFIED` e perfil `SHELTER` ativo envia `PUT /v2/pets/{id}` para um pet cujo `guardianProfile` é nulo
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
