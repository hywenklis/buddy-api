package com.buddy.api.web.authetication.controllers;

import com.buddy.api.commons.configurations.security.cookies.CookieManager;
import com.buddy.api.domains.authentication.dtos.AuthDto;
import com.buddy.api.domains.authentication.services.AuthService;
import com.buddy.api.web.authetication.mappers.AuthenticationMapper;
import com.buddy.api.web.authetication.requests.AuthRequest;
import com.buddy.api.web.authetication.responses.AuthResponse;
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
public class AuthController implements AuthControllerDoc {

    private final AuthService authenticateService;
    private final AuthenticationMapper mapper;
    private final CookieManager cookieManager;

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
