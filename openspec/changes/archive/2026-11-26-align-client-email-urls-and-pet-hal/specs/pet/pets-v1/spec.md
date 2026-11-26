# Pet V1 HAL Collection Relation Specification

## Purpose

Desacoplar a chave `_embedded` retornada pelo endpoint paginado legado `GET /v1/pets` do nome interno da classe Java `PetParamsResponse`, fixando a relação semântica `"pets"` via Spring HATEOAS.

## ADDED Requirements

### Requirement: Stable HAL Embedded Key `"pets"` on GET /v1/pets
O record `PetParamsResponse` (`com.buddy.api.web.pets.responses.PetParamsResponse`) MUST ser anotado com `@Relation(collectionRelation = "pets")` para que `PagedResourcesAssembler` no `FindPetController` serialize a coleção sob `$._embedded.pets` em vez do nome derivado da classe (`$._embedded.petParamsResponseList`).

#### Scenario: Listagem paginada retorna coleção sob `$._embedded.pets`
- **WHEN** o cliente envia `GET /v1/pets` e existem pets correspondentes aos filtros
- **THEN** a resposta `200 OK` SHALL conter a lista de itens no campo JSON `$._embedded.pets`
- **AND** a chave legada `$._embedded.petParamsResponseList` SHALL NOT estar presente no payload.

#### Scenario: Listagem vazia mantém comportamento padrão do Spring HATEOAS
- **WHEN** o cliente envia `GET /v1/pets` e não há pets cadastrados ou compatíveis com os filtros
- **THEN** a resposta `200 OK` SHALL omitir `_embedded` mantendo os metadados de `page` e `_links`.
