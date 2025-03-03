package com.buddy.api.commons.configuration.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(String secretKey,
                             Long accessTokenExpiration,
                             Long refreshTokenExpiration,
                             List<OriginConfig> allowedOrigins) {

    public record OriginConfig(
        String type,
        String code
    ) {}
}
