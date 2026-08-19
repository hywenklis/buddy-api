package com.buddy.api.web.authentication.controllers;

import com.buddy.api.commons.configurations.cache.annotations.RateLimited;
import com.buddy.api.commons.configurations.security.cookies.annotations.ClearCookiesOnSuccess;
import com.buddy.api.commons.configurations.security.jwt.JwtUtil;
import com.buddy.api.domains.authentication.dtos.AuthDto;
import com.buddy.api.domains.authentication.services.AuthService;
import com.buddy.api.web.authentication.mappers.AuthenticationMapper;
import com.buddy.api.web.authentication.requests.AuthRequest;
import com.buddy.api.web.authentication.responses.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AuthControllerDoc {

    private final AuthService authenticateService;
    private final AuthenticationMapper mapper;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @RateLimited(
        operation = "login",
        emailSpel = "#request.email",
        limitMessage = "Too many login attempts. Please wait a minute before trying again."
    )
    public AuthResponse authenticate(
        @Valid @RequestBody final AuthRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthDto authDto = authenticateService.authenticate(mapper.toAuthDto(request));
        return mapper.toAuthResponse(authDto);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse refreshToken(
        final HttpServletRequest request
    ) {
        AuthDto authDto = authenticateService.refreshToken(request);
        return mapper.toAuthResponse(authDto);
    }

    @ClearCookiesOnSuccess
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(final HttpServletRequest request) {
        authenticateService.logoutComplete(request);
    }
}
