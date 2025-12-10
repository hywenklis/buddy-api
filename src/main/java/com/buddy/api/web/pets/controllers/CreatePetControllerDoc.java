package com.buddy.api.web.pets.controllers;

import com.buddy.api.web.advice.error.ErrorResponse;
import com.buddy.api.web.defaultresponses.CreatedSuccessResponse;
import com.buddy.api.web.pets.requests.PetRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

@Tag(name = "Pet", description = "Endpoint related to pet registration")
public interface CreatePetControllerDoc {

    @Operation(summary = "Register pet",
        description = "Register pet with their appropriate information", responses = {
            @ApiResponse(responseCode = "201", description = "Pet created successfully"),

            @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))})

    CreatedSuccessResponse registration(PetRequest petRequest);
}
