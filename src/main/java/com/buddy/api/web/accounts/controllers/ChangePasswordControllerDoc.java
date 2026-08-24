package com.buddy.api.web.accounts.controllers;

import com.buddy.api.web.accounts.requests.ChangePasswordRequest;
import com.buddy.api.web.advice.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "Accounts")
public interface ChangePasswordControllerDoc {

    @Operation(
        summary = "Change the password of the authenticated user",
        description = "Validates the current password, sets the new password, revokes all current "
            + "tokens, and triggers a notification email.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Password changed successfully."
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or current password.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid token.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    void changePassword(ChangePasswordRequest request,
                        HttpServletRequest httpRequest,
                        UserDetails userDetails);
}
