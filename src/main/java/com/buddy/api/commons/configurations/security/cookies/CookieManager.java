package com.buddy.api.commons.configurations.security.cookies;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookieManager {

    private final CookieFactory cookieFactory;

    public void handleCookies(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final String accessToken,
        final String refreshToken
    ) {
        log.info("Adding authentication cookies for client response");
        cookieFactory.addAuthCookies(response, accessToken, refreshToken);
    }

    public void clearCookies(final HttpServletResponse response) {
        log.info("Clearing authentication cookies");
        cookieFactory.invalidateAuthCookies(response);
    }
}

