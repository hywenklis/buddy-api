package com.buddy.api.commons.configurations.security.cookies;

import com.buddy.api.commons.configurations.properties.AuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieFactory {

    private static final String ACCESS_TOKEN_NAME = "access_token";
    private static final String REFRESH_TOKEN_NAME = "refresh_token";
    private static final String SAME_SITE_ATTRIBUTE = "SameSite";
    private static final String SAME_SITE_STRICT = "Strict";

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
        Cookie cookie = new Cookie(name, value != null ? value : "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute(SAME_SITE_ATTRIBUTE, SAME_SITE_STRICT);
        response.addCookie(cookie);
    }
}
