package com.buddy.api.commons.configurations.properties;

import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
@Builder
public record AuthProperties(String secretKey,
                             Integer accessTokenExpiration,
                             Integer refreshTokenExpiration) {

    public Integer getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration / 1000;
    }

    public Integer getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpiration / 1000;
    }
}

