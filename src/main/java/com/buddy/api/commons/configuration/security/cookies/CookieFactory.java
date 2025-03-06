package com.buddy.api.commons.configuration.security.cookies;

import com.buddy.api.commons.configuration.properties.AuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieFactory {

    private static final String ACCESS_TOKEN_NAME = "access_token";
    private static final String REFRESH_TOKEN_NAME = "refresh_token";
    private static final String SET_COOKIE_HEADER = "Set-Cookie";
    private static final String COOKIE_ATTRIBUTES = "Path=/; HttpOnly; Secure; SameSite=Strict";

    private final AuthProperties authProperties;

    public void addAuthCookies(final HttpServletResponse response,
                               final String accessToken,
                               final String refreshToken
    ) {
        addCookie(response, ACCESS_TOKEN_NAME, accessToken,
            authProperties.getAccessTokenExpirationInSeconds());

        addCookie(response, REFRESH_TOKEN_NAME, refreshToken,
            authProperties.getRefreshTokenExpirationInSeconds());
    }

    public void invalidateAuthCookies(final HttpServletResponse response) {
        addCookie(response, ACCESS_TOKEN_NAME, null, 0);
        addCookie(response, REFRESH_TOKEN_NAME, null, 0);
    }

    private void addCookie(final HttpServletResponse response,
                           final String name,
                           final String value,
                           final int maxAge
    ) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);

        String cookieHeader = String.format("%s=%s; Max-Age=%d; %s",
            name, value != null ? value : "", maxAge, COOKIE_ATTRIBUTES);
        response.addHeader(SET_COOKIE_HEADER, cookieHeader);
    }
}
