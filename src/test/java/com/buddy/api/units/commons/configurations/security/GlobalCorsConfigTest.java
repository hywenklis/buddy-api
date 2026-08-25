package com.buddy.api.units.commons.configurations.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.commons.configurations.properties.BuddySecurityProperties;
import com.buddy.api.commons.configurations.security.GlobalCorsConfig;
import com.buddy.api.units.UnitTestAbstract;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.filter.CorsFilter;

class GlobalCorsConfigTest extends UnitTestAbstract {

    private static final String KEY = "12345678901234567890123456789012";

    @Test
    @DisplayName("Should create CorsFilter when cors properties are present")
    void corsFilter_withConfiguredOrigins_shouldCreateFilter() {
        final var cors = new BuddySecurityProperties.CorsProperties(
            List.of("http://localhost:3000", "https://buddyclient.vercel.app")
        );
        final var properties = new BuddySecurityProperties(KEY, "AES/GCM/NoPadding", 128, 12, cors);
        final var config = new GlobalCorsConfig(properties);

        final CorsFilter filter = config.corsFilter();

        assertThat(filter).isNotNull();
    }

    @Test
    @DisplayName("Should create CorsFilter when cors properties or allowed origins are null")
    void corsFilter_withNullOrigins_shouldCreateFilter() {
        final var propertiesNullCors = new BuddySecurityProperties(
            KEY, "AES/GCM/NoPadding", 128, 12, null
        );
        final var config1 = new GlobalCorsConfig(propertiesNullCors);

        assertThat(config1.corsFilter()).isNotNull();

        final var corsNullOrigins = new BuddySecurityProperties.CorsProperties(null);
        final var propertiesNullOrigins = new BuddySecurityProperties(
            KEY, "AES/GCM/NoPadding", 128, 12, corsNullOrigins
        );
        final var config2 = new GlobalCorsConfig(propertiesNullOrigins);

        assertThat(config2.corsFilter()).isNotNull();
    }
}
