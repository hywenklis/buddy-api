package com.buddy.api.web.pets.controllers;

import com.buddy.api.domains.pet.services.FindPet;
import com.buddy.api.web.pets.mappers.PetMapperParamsResponse;
import com.buddy.api.web.pets.requests.PetSearchCriteriaRequest;
import com.buddy.api.web.pets.responses.PetParamsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Pet", description = "Endpoints related to pets")
public class FindPetController {

    private final FindPet findPetService;
    private final PetMapperParamsResponse mapperResponse;
    private final PagedResourcesAssembler<PetParamsResponse> pagedResourcesAssembler;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get pets with pagination",
        description = "Get pets based on search criteria with pagination. "
            + "You can provide various search parameters to filter the results."
    )
    public PagedModel<EntityModel<PetParamsResponse>> findPetsBySearchParams(
        @Parameter(description = "Search criteria for filtering pets")
        final PetSearchCriteriaRequest petSearchCriteriaRequest,
        @Parameter(description = "Pagination information")
        final Pageable pageable
    ) {
        var petPage = findPetService.findPets(petSearchCriteriaRequest, pageable);
        return pagedResourcesAssembler.toModel(petPage.map(mapperResponse::mapToParamsResponse));
    }
}
