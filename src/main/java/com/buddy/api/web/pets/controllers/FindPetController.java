package com.buddy.api.web.pets.controllers;

import com.buddy.api.domains.pet.services.FindPet;
import com.buddy.api.web.pets.mappers.PetMapperParamsResponse;
import com.buddy.api.web.pets.requests.PetSearchCriteriaRequest;
import com.buddy.api.web.pets.responses.PetParamsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/pets")
@RequiredArgsConstructor
public class FindPetController implements FindPetControllerDoc {

    private final FindPet findPetService;
    private final PetMapperParamsResponse mapperResponse;
    private final PagedResourcesAssembler<PetParamsResponse> pagedResourcesAssembler;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PagedModel<EntityModel<PetParamsResponse>> findPetsBySearchParams(
        final PetSearchCriteriaRequest petSearchCriteriaRequest,
        final Pageable pageable
    ) {
        var petPage = findPetService.findPets(petSearchCriteriaRequest, pageable);
        return pagedResourcesAssembler.toModel(petPage.map(mapperResponse::mapToParamsResponse));
    }
}
