package com.buddy.api.web.authetication.controllers;

import com.buddy.api.web.advice.error.ErrorResponse;
import com.buddy.api.web.authetication.requests.AuthRequest;
import com.buddy.api.web.authetication.responses.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

public interface AuthControllerDoc {
    @Operation(
        summary = "Authenticate a user",
        description = "Authenticates a user with email and password, "
            + "returning access and refresh tokens in cookies and response body."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User authenticated successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthResponse.class)
            )),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request (e.g., missing email or password)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (e.g., invalid credentials, blocked/deleted account)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            ))})
    AuthResponse authenticate(
        AuthRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse response
    );

    @Operation(
        summary = "Refresh authentication token",
        description = "Refreshes the access token using a valid refresh token provided in a cookie."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthResponse.class)
            )),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (e.g., missing or invalid refresh token)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            ))})
    AuthResponse refreshToken(
        final HttpServletRequest request,
        final HttpServletResponse response
    );
}
