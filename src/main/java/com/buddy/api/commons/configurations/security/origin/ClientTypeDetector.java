package com.buddy.api.commons.configurations.security.origin;

import com.buddy.api.commons.configurations.properties.AuthProperties;
import com.buddy.api.commons.configurations.security.origin.enums.ClientType;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientTypeDetector {

    public static final String X_ORIGIN_CODE_HEADER = "X-Origin-Code";
    public static final String ORIGIN_HEADER = "Origin";

    private final AuthProperties authProperties;

    public ClientType detectClientType(final HttpServletRequest request) {
        final var origin = Optional.ofNullable(request.getHeader(X_ORIGIN_CODE_HEADER))
            .filter(header -> !header.isBlank())
            .orElseGet(() -> request.getHeader(ORIGIN_HEADER));

        log.debug("Detecting client type for origin: {}", origin);

        if (origin == null) {
            return ClientType.UNKNOWN;
        }

        return authProperties.allowedOrigins().stream()
            .filter(config -> config.code().equals(origin))
            .findFirst()
            .map(config -> ClientType.fromString(config.type()))
            .orElse(ClientType.UNKNOWN);
    }
}