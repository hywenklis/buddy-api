package com.buddy.api.web.terms.controllers;

import com.buddy.api.web.terms.responses.TermsVersionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "Terms", description = "Endpoints related to terms of use")
public interface TermsControllerDoc {

    @Operation(summary = "Get active terms",
        description = "Retrieve the currently active terms of use")
    @ApiResponse(responseCode = "200", description = "Terms retrieved successfully")
    TermsVersionResponse getActiveTerms();

    @Operation(summary = "Accept terms",
        description = "Register acceptance of active terms")
    @ApiResponse(responseCode = "204", description = "Terms accepted successfully")
    void acceptTermsVersion(UserDetails userDetails, HttpServletRequest request);
}
