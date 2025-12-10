package com.buddy.api.web.profiles.controllers;

import com.buddy.api.web.advice.error.ErrorResponse;
import com.buddy.api.web.defaultresponses.CreatedSuccessResponse;
import com.buddy.api.web.profiles.requests.ProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

@Tag(name = "Profile", description = "Endpoint related to profile registration")
public interface CreateProfileControllerDoc {

    @Operation(summary = "Register profile",
        description = "Register profile with their appropriate information", responses = {
            @ApiResponse(responseCode = "201", description = "Profile created successfully"),

            @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "404", description = "Account not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))})

    CreatedSuccessResponse registration(ProfileRequest request);
}
