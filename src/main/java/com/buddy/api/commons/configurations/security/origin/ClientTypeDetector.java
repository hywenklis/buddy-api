package com.buddy.api.commons.configurations.security.origin;

import com.buddy.api.commons.configurations.properties.AuthProperties;
import com.buddy.api.commons.configurations.security.origin.enums.ClientType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientTypeDetector {

    private final AuthProperties authProperties;

    public ClientType detectClientType(final HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        log.debug("Detecting client type for origin: {}", origin);

        if (origin == null) {
            return ClientType.UNKNOWN;
        }

        for (AuthProperties.OriginConfig config : authProperties.allowedOrigins()) {
            if (config.code().equals(origin)) {
                return ClientType.fromString(config.type());
            }
        }

        return ClientType.UNKNOWN;
    }
}