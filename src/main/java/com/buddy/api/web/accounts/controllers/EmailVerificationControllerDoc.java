package com.buddy.api.web.accounts.controllers;

import com.buddy.api.domains.authentication.dtos.AuthenticatedUser;
import com.buddy.api.web.accounts.requests.ConfirmEmailRequest;
import com.buddy.api.web.advice.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

@Tag(name = "Account Verification", description = "Endpoints related to email verification")
public interface EmailVerificationControllerDoc {

    @Operation(summary = "Request email verification",
        description = "Request a new email verification token", responses = {
            @ApiResponse(responseCode = "202", description = "Verification email requested"),

            @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))})

    void requestVerification(AuthenticatedUser user);

    @Operation(summary = "Confirm email",
        description = "Confirm email address using the token", responses = {
            @ApiResponse(responseCode = "200", description = "Email confirmed successfully"),

            @ApiResponse(responseCode = "400", description = "Invalid token",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))})

    void confirm(ConfirmEmailRequest request, AuthenticatedUser user);
}
