package com.buddy.api.commons.configurations.security;

import com.buddy.api.commons.configurations.properties.BuddySecurityProperties;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@RequiredArgsConstructor
public class GlobalCorsConfig {

    private final BuddySecurityProperties securityProperties;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        Optional.ofNullable(securityProperties.cors())
            .map(BuddySecurityProperties.CorsProperties::allowedOrigins)
            .ifPresent(origins -> origins.forEach(config::addAllowedOriginPattern));
        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
