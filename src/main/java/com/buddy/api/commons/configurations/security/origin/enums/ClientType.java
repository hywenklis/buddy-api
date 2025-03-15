package com.buddy.api.commons.configurations.security.origin.enums;

import java.util.Arrays;

public enum ClientType {
    WEB, MOBILE, TOOLS, UNKNOWN;

    public static ClientType fromString(final String typeRequestOrigin) {
        return Arrays.stream(ClientType.values())
            .filter(type -> type.name().equals(typeRequestOrigin))
            .findFirst()
            .orElse(UNKNOWN);
    }
}
