package com.buddy.api.domains.authentication.services;

import com.buddy.api.domains.authentication.dtos.AuthDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthDto authenticate(AuthDto authDto);

    AuthDto refreshToken(HttpServletRequest request);
}
