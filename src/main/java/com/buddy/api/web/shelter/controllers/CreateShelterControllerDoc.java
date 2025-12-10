package com.buddy.api.web.shelter.controllers;

import com.buddy.api.web.advice.error.ErrorResponse;
import com.buddy.api.web.defaultresponses.CreatedSuccessResponse;
import com.buddy.api.web.shelter.requests.ShelterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

@Tag(name = "Shelter", description = "Endpoint related to shelter registration")
public interface CreateShelterControllerDoc {

    @Operation(summary = "Register shelter",
        description = "Register shelter with their appropriate information", responses = {
            @ApiResponse(responseCode = "201", description = "Shelter created successfully"),

            @ApiResponse(
                responseCode = "400",
                description = "Invalid input data (e.g. CPF/Email already exists)",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))})

    CreatedSuccessResponse registration(ShelterRequest shelterRequest);
}
