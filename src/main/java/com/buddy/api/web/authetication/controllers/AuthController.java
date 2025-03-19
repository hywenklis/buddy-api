package com.buddy.api.web.authetication.controllers;

import com.buddy.api.commons.configurations.security.cookies.CookieManager;
import com.buddy.api.domains.authentication.dtos.AuthDto;
import com.buddy.api.domains.authentication.services.AuthService;
import com.buddy.api.web.advice.error.ErrorResponse;
import com.buddy.api.web.authetication.mappers.AuthenticationMapper;
import com.buddy.api.web.authetication.requests.AuthRequest;
import com.buddy.api.web.authetication.responses.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and token refresh")
public class AuthController {

    private final AuthService authenticateService;
    private final AuthenticationMapper mapper;
    private final CookieManager cookieManager;

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
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request (e.g., missing email or password)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (e.g., invalid credentials, blocked/deleted account)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse authenticate(
        @Valid @RequestBody final AuthRequest request,
        final HttpServletRequest httpRequest,
        final HttpServletResponse response
    ) {
        AuthDto authDto = authenticateService.authenticate(mapper.toAuthDto(request));

        cookieManager.handleCookies(
            httpRequest,
            response,
            authDto.accessToken(),
            authDto.refreshToken()
        );

        return mapper.toAuthResponse(authDto);
    }

    @Operation(
        summary = "Refresh authentication token",
        description = "Refreshes the access token using a valid refresh token provided in a cookie."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (e.g., missing or invalid refresh token)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse refreshToken(
        final HttpServletRequest request,
        final HttpServletResponse response
    ) {
        AuthDto authDto = authenticateService.refreshToken(request);
        cookieManager.handleCookies(
            request,
            response,
            authDto.accessToken(),
            authDto.refreshToken()
        );

        return mapper.toAuthResponse(authDto);
    }
}
