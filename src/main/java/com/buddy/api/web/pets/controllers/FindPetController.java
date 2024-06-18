package com.buddy.api.web.pets.controllers;

import com.buddy.api.domains.pet.services.FindPet;
import com.buddy.api.web.pets.mappers.PetMapperParamsResponse;
import com.buddy.api.web.pets.requests.PetSearchCriteriaRequest;
import com.buddy.api.web.pets.responses.PetParamsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private final FindPet service;
    private final PetMapperParamsResponse mapperResponse;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get pets",
            description = "Get pets based on search criteria with pagination"
    )
    public Page<PetParamsResponse> findPetsBySearchParams(PetSearchCriteriaRequest searchCriteria,
                                                          Pageable pageable) {
        return service.findPets(searchCriteria, pageable)
                .map(mapperResponse::mapToParamsResponse);
    }
}
