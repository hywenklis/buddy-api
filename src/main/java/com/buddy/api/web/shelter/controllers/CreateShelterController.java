package com.buddy.api.web.shelter.controllers;

import com.buddy.api.domains.shelter.services.CreateShelter;
import com.buddy.api.web.shelter.mappers.ShelterMapperRequest;
import com.buddy.api.web.shelter.mappers.ShelterMapperResponse;
import com.buddy.api.web.shelter.requests.ShelterRequest;
import com.buddy.api.web.shelter.responses.ShelterResponse;
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
@RequestMapping("/v1/shelters")
@RequiredArgsConstructor
@Tag(name = "Shelter", description = "Endpoint related to shelter registration")
public class CreateShelterController {

    private final CreateShelter service;
    private final ShelterMapperRequest mapperRequest;
    private final ShelterMapperResponse mapperResponse;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Register shelter",
        description = "Register shelter with their appropriate information"
    )
    public ShelterResponse registration(@RequestBody @Valid ShelterRequest shelterRequest) {
        service.create(mapperRequest.mapToDto(shelterRequest));
        return mapperResponse.mapToResponse();
    }
}
