package com.buddy.api.commons.http;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HttpRequestExtractor {

    private static final String UNKNOWN_AGENT = "Unknown";

    @Value("${buddy.security.trusted-proxy-addresses:}")
    private List<String> trustedProxyAddresses = List.of();

    public String extractIp(final HttpServletRequest request) {
        return ClientIpResolver.extract(request, trustedProxyAddresses);
    }

    public String extractUserAgent(final HttpServletRequest request) {
        return Optional.ofNullable(request)
            .map(req -> req.getHeader("User-Agent"))
            .filter(Predicate.not(String::isBlank))
            .orElse(UNKNOWN_AGENT);
    }
}
