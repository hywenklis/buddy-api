package com.buddy.api.commons.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secretKey,
                            Long accessTokenExpiration,
                            Long refreshTokenExpiration) {
}
