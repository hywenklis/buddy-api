package com.buddy.api.web.accounts.controllers;

import com.buddy.api.web.accounts.requests.AccountRequest;
import com.buddy.api.web.advice.error.ErrorResponse;
import com.buddy.api.web.defaultresponses.CreatedSuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

@Tag(name = "Account", description = "Endpoint related to account registration")
public interface CreateAccountControllerDoc {

    @Operation(
        summary = "Register account",
        description = "Register account with their appropriate information",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Account created successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
        })
    CreatedSuccessResponse registration(AccountRequest accountRequest);
}
