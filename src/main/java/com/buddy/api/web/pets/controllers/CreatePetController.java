package com.buddy.api.web.pets.controllers;

import com.buddy.api.domains.pet.dtos.PetDto;
import com.buddy.api.domains.pet.services.CreatePet;
import com.buddy.api.web.defaultresponses.CreatedSuccessResponse;
import com.buddy.api.web.pets.mappers.PetMapperRequest;
import com.buddy.api.web.pets.requests.PetRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/pets")
@RequiredArgsConstructor
@Tag(name = "Pet", description = "Endpoint related to pet registration")
public class CreatePetController {

    private final CreatePet service;
    private final PetMapperRequest mapperRequest;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Register pet",
        description = "Register pet with their appropriate information"
    )
    public CreatedSuccessResponse registration(@RequestBody @Valid final PetRequest petRequest) {
        PetDto petDto = mapperRequest.mapToDto(petRequest);
        service.create(petDto);
        return new CreatedSuccessResponse();
    }
}
