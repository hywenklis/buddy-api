# Proposta: Alinhamento de URLs de E-mail e Relação HAL `_embedded` da API V1 com o `buddy-client`

## Por quê (Why)

Durante a revisão da PR #142 no `buddy-client` (`feat/auth-lifecycle-and-terms`), foram apontados dois desacoplamentos/desalinhamentos entre os contratos configurados no `buddy-api` e o frontend:
1. **URLs dos templates de e-mail:** `application-local.yml` e `application-test.yml` configuram `https://buddyclient.vercel.app/confirm-email?token=` e `https://buddyclient.vercel.app/reset-password?token=`, enquanto as rotas canônicas do `buddy-client` são `/auth/verify-email` e `/auth/reset-password`.
2. **Acoplamento ao nome da classe Java no HAL `_embedded` da V1 (`GET /v1/pets`):** `PagedResourcesAssembler<PetParamsResponse>` deriva a chave `$._embedded.petParamsResponseList` do nome da classe Java `PetParamsResponse`, acoplando consumidores a refatorações internas de DTO. Como o `buddy-client` já foi tornado resiliente no commit `c5eb48b`, agora podemos fixar `@Relation(collectionRelation = "pets")` no `buddy-api` com segurança.

## O que muda (What Changes)

- **Configuração de URLs de e-mail (`account` / `commons.configurations.properties`):**
  - Atualizar `buddy.email.templates.verification.url` para `https://buddyclient.vercel.app/auth/verify-email?token=` em `application-local.yml` e `application-test.yml`.
  - Atualizar `buddy.email.templates.forgot-password.url` para `https://buddyclient.vercel.app/auth/reset-password?token=` em `application-local.yml` e `application-test.yml`.
  - Adicionar teste de integração validando o binding de `EmailProperties` com as novas URLs canônicas.
- **Relação HAL `_embedded` estável em `GET /v1/pets` (`web.pets`):**
  - Anotar `PetParamsResponse` com `@Relation(collectionRelation = "pets")` para que `PagedResourcesAssembler` serialize `$._embedded.pets` em vez de `$._embedded.petParamsResponseList`.
  - Atualizar `IntegrationTestAbstract` e `FindPetControllerTest` para validar `$._embedded.pets` (e ausência de `petParamsResponseList`).

## Bounded Contexts e Camadas Impactadas

- **Configuração transversal (`commons.configurations.properties`):** `src/main/resources/application-local.yml`, `src/test/resources/application-test.yml`, `EmailProperties`.
- **Camada Web (`web.pets`):** `src/main/java/com/buddy/api/web/pets/responses/PetParamsResponse.java`.
- **Testes (`integrations/`):** `IntegrationTestAbstract.java`, `FindPetControllerTest.java` e verificação automatizada das URLs de `EmailProperties`.

## Fora de Escopo (Out of Scope)

- **Configuração de CORS em produção:** o ambiente de produção continua restrito a `https://buddyclient.vercel.app` (nenhum ajuste de `allowed-origins`).
- **Política de autenticação de `/v1/accounts/verifications/{request,confirm}`:** permanece fora desta change.
- **Contrato de `GET /v2/pets`:** a API V2 já retorna `PageResponse` padronizado sem HAL `_embedded` e não é alterada aqui.
- **Variáveis de ambiente do servidor publicado:** caso o ambiente publicado sobrescreva `buddy.email.templates.*.url` externamente, a atualização em runtime será feita no deploy.
