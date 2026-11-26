# Tarefas: Alinhamento de URLs de E-mail e `@Relation(collectionRelation = "pets")`

## 1. Alinhamento das URLs dos templates de e-mail

- [x] Atualizar `buddy.email.templates.verification.url` e `buddy.email.templates.forgot-password.url` em `src/main/resources/application-local.yml` para `https://buddyclient.vercel.app/auth/verify-email?token=` e `https://buddyclient.vercel.app/auth/reset-password?token=`.
- [x] Atualizar as mesmas propriedades em `src/test/resources/application-test.yml` mantendo paridade entre perfis `local` e `test`.
- [x] Criar teste em `src/test/java/com/buddy/api/integrations/commons/configurations/properties/EmailPropertiesIntegrationTest.java` (estendendo `IntegrationTestAbstract`, AssertJ + `@DisplayName`, zero comentários) validando o binding das URLs canônicas em `EmailProperties`.

## 2. Estabilização da relação HAL `_embedded` em `GET /v1/pets`

- [x] Adicionar `@Relation(collectionRelation = "pets")` em `src/main/java/com/buddy/api/web/pets/responses/PetParamsResponse.java` respeitando ordem alfabética de imports (Checkstyle).
- [x] Atualizar `EMBEDDED_PET_RESPONSES` para `"$._embedded.pets"` em `src/test/java/com/buddy/api/integrations/IntegrationTestAbstract.java`.
- [x] Atualizar `EMBEDDED_PET_RESPONSES` para `"$._embedded.pets"` em `src/test/java/com/buddy/api/integrations/web/pet/controller/FindPetControllerTest.java` e validar ausência de `$._embedded.petParamsResponseList`.

## 3. Quality Gate e Validação Final

- [x] Executar `./gradlew.bat build` garantindo todos os testes passando e 0 violações de Checkstyle (`maxWarnings = 0`), SpotBugs e PMD 7.22.0.
- [x] Validar a change OpenSpec com `openspec validate align-client-email-urls-and-pet-hal --strict`.
