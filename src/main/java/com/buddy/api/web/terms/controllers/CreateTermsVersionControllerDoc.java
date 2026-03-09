package com.buddy.api.web.terms.controllers;

import com.buddy.api.web.terms.requests.CreateTermsVersionRequest;
import com.buddy.api.web.terms.responses.TermsVersionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "Terms Admin", description = "Administrative operations for Terms of Use management")
public interface CreateTermsVersionControllerDoc {

    @Operation(summary = "Create a new Terms of Use version",
        description =
            "Creates a new version of the Terms of Use Only ADMIN users can perform this "
                + "operation. If isActive is true, "
                + "all previous active versions will be deactivated.",
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Terms version created successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TermsVersionResponse.class))),
        @ApiResponse(responseCode = "400",
            description = "Invalid request body - missing or invalid fields", content = @Content),
        @ApiResponse(responseCode = "401",
            description = "Unauthorized - missing or invalid JWT token", content = @Content),
        @ApiResponse(responseCode = "403",
            description = "Forbidden - user does not have ADMIN role", content = @Content),
        @ApiResponse(responseCode = "409",
            description = "Conflict - version tag already exists", content = @Content)})
    TermsVersionResponse createVersion(UserDetails userDetails, CreateTermsVersionRequest request);
}
