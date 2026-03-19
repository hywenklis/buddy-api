package com.buddy.api.web.terms.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;

@Tag(name = "Terms Admin", description = "Administrative operations for Terms of Use management")
public interface ActivateTermsVersionControllerDoc {

    @Operation(summary = "Activate a Terms of Use version",
        description = "Activates the specified Terms of Use version, "
            + "deactivating all other versions. "
            + "Only one version can be active at a time. "
            +
            "Only ADMIN users can perform this operation.",
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204",
            description = "Terms version activated successfully",
            content = @Content),
        @ApiResponse(responseCode = "401",
            description = "Unauthorized - missing or invalid JWT token", content = @Content),
        @ApiResponse(responseCode = "403",
            description = "Forbidden - user does not have ADMIN role", content = @Content),
        @ApiResponse(responseCode = "404",
            description = "Not Found - terms version does not exist", content = @Content)})
    void activateVersion(
        @Parameter(description = "UUID of the terms version to activate", required = true)
        UUID termsVersionId);
}
