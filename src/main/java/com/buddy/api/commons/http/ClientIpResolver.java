package com.buddy.api.commons.http;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public final class ClientIpResolver {

    private static final String UNKNOWN_IP = "0.0.0.0";

    private ClientIpResolver() {
    }

    public static String extract(final HttpServletRequest request,
                                 final Collection<String> trustedProxyAddresses) {
        if (request == null) {
            return UNKNOWN_IP;
        }

        final String remoteAddress = StringUtils.trimToEmpty(request.getRemoteAddr());
        if (!isTrustedProxy(remoteAddress, trustedProxyAddresses)) {
            return StringUtils.defaultIfBlank(remoteAddress, UNKNOWN_IP);
        }

        return Optional.ofNullable(firstHeaderValue(request.getHeader("X-Forwarded-For")))
            .or(() -> Optional.ofNullable(firstHeaderValue(request.getHeader("X-Real-IP"))))
            .orElse(StringUtils.defaultIfBlank(remoteAddress, UNKNOWN_IP));
    }

    private static boolean isTrustedProxy(final String remoteAddress,
                                          final Collection<String> trustedProxyAddresses) {
        return StringUtils.isNotBlank(remoteAddress)
            && trustedProxyAddresses != null
            && trustedProxyAddresses.stream()
                .filter(StringUtils::isNotBlank)
                .map(StringUtils::trim)
                .anyMatch(remoteAddress::equals);
    }

    private static String firstHeaderValue(final String header) {
        return Optional.ofNullable(StringUtils.trimToNull(header))
            .map(value -> value.split(",", 2)[0])
            .map(StringUtils::trimToNull)
            .orElse(null);
    }
}
