package com.buddy.api.commons.configurations.security.origin.enums;

import java.util.Arrays;
import java.util.Locale;

public enum ClientType {
    WEB, MOBILE, TOOLS, UNKNOWN;

    public static ClientType fromString(final String type) {
        return Arrays.stream(ClientType.values())
            .filter(t -> t.name().equals(type.toLowerCase(Locale.ROOT)))
            .findFirst()
            .orElse(UNKNOWN);
    }

    public boolean shouldGenerateCookies() {
        return this == WEB || this == TOOLS;
    }
}
