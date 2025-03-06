package com.buddy.api.commons.configurations.security.origin.enums;

import java.util.Locale;

public enum ClientType {
    WEB, MOBILE, TOOLS, UNKNOWN;

    public static ClientType fromString(final String type) {
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "web" -> WEB;
            case "mobile" -> MOBILE;
            case "tools" -> TOOLS;
            default -> UNKNOWN;
        };
    }

    public boolean shouldGenerateCookies() {
        return this == WEB || this == TOOLS;
    }
}
