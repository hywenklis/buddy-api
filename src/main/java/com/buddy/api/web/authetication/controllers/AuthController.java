package com.buddy.api.web.authetication.controllers;

import com.buddy.api.domains.authentication.dtos.AuthDto;
import com.buddy.api.domains.authentication.services.AuthenticationService;
import com.buddy.api.web.authetication.mappers.AuthenticationMapper;
import com.buddy.api.web.authetication.requests.AuthRequest;
import com.buddy.api.web.authetication.responses.AuthResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
public class AuthController {

    private final AuthenticationService accountService;
    private final AuthenticationMapper mapper;

    @PostMapping
    @ResponseStatus(value = HttpStatus.OK)
    public AuthResponse authenticate(@RequestBody final AuthRequest request,
                                     final HttpServletResponse response
    ) {
        AuthDto authDto = accountService.authenticate(mapper.toAuthDto(request));

        // TODO: Extrair código para uma classe utilitária e tranformar magic number em propriedades
        Cookie accessCookie = new Cookie("access_token", authDto.accessToken());
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(15 * 60);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refresh_token", authDto.refreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(24 * 60 * 60);
        response.addCookie(refreshCookie);

        return mapper.toAuthResponse(authDto);
    }
}
