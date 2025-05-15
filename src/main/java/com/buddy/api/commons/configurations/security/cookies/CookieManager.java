package com.buddy.api.commons.configurations.security.cookies;

import com.buddy.api.commons.configurations.security.origin.ClientTypeDetector;
import com.buddy.api.commons.configurations.security.origin.enums.ClientType;
import com.buddy.api.commons.exceptions.InvalidClientOriginException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookieManager {

    private final ClientTypeDetector clientTypeDetector;
    private final CookieFactory cookieFactory;

    private final Map<ClientType, BiConsumer<HttpServletResponse, CookieData>> cookieActions =
        Map.of(
            ClientType.WEB, this::addCookies,
            ClientType.TOOLS, this::addCookies,
            ClientType.MOBILE, this::invalidateCookies,
            ClientType.UNKNOWN, this::throwInvalidOriginException
        );

    public void handleCookies(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final String accessToken,
        final String refreshToken
    ) {
        ClientType clientType = clientTypeDetector.detectClientType(request);
        log.debug("Handling cookies for client type: {}", clientType);

        CookieData cookieData = new CookieData(accessToken, refreshToken);
        cookieActions.getOrDefault(clientType, this::invalidateCookies)
            .accept(response, cookieData);
    }

    private void addCookies(final HttpServletResponse response, final CookieData cookieData) {
        log.info("Adding authentication cookies for client");
        cookieFactory.addAuthCookies(response, cookieData.accessToken(), cookieData.refreshToken());
    }

    private void invalidateCookies(final HttpServletResponse response,
                                   final CookieData cookieData
    ) {
        log.info("Invalidating authentication cookies for client");
        cookieFactory.invalidateAuthCookies(response);
    }

    private void throwInvalidOriginException(final HttpServletResponse response,
                                             final CookieData cookieData
    ) {
        log.warn("Invalid origin detected for request");
        throw new InvalidClientOriginException("Invalid origin detected");
    }
}
