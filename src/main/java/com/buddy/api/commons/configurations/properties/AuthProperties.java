package com.buddy.api.commons.configurations.properties;

import java.util.List;
import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
@Builder
public record AuthProperties(String secretKey,
                             Integer accessTokenExpiration,
                             Integer refreshTokenExpiration,
                             List<OriginConfig> allowedOrigins) {

    @Builder
    public record OriginConfig(
        String type,
        String code
    ) { }

    public Integer getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration / 1000;
    }

    public Integer getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpiration / 1000;
    }
}
