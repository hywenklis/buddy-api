package com.buddy.api.commons.http;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class HttpRequestExtractor {

    private static final String UNKNOWN_IP = "0.0.0.0";
    private static final String UNKNOWN_AGENT = "Unknown";

    public String extractIp(final HttpServletRequest request) {
        return Optional.ofNullable(request)
            .map(req -> StringUtils.defaultIfBlank(req.getHeader("X-Forwarded-For"), null))
            .map(header -> header.split(",")[0].trim())
            .or(() -> Optional.ofNullable(request)
                .map(req -> StringUtils.defaultIfBlank(req.getHeader("X-Real-IP"), null)))
            .or(() -> Optional.ofNullable(request)
                .map(req -> StringUtils.defaultIfBlank(req.getRemoteAddr(), null)))
            .orElse(UNKNOWN_IP);
    }

    public String extractUserAgent(final HttpServletRequest request) {
        return Optional.ofNullable(request)
            .map(req -> req.getHeader("User-Agent"))
            .filter(Predicate.not(String::isBlank))
            .orElse(UNKNOWN_AGENT);
    }
}
