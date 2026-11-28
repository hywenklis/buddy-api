package com.buddy.api.integrations.commons.configurations.properties;

import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.commons.configurations.properties.EmailProperties;
import com.buddy.api.integrations.IntegrationTestAbstract;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("EmailProperties integration")
class EmailPropertiesIntegrationTest extends IntegrationTestAbstract {

    @Autowired
    private EmailProperties emailProperties;

    @Test
    @DisplayName("Should bind canonical buddy-client URLs for verification and password reset")
    void shouldBindCanonicalClientUrls() {
        assertThat(emailProperties.templates().verification().url())
            .isEqualTo("https://buddyclient.vercel.app/auth/verify-email?token=");
        assertThat(emailProperties.templates().forgotPassword().url())
            .isEqualTo("https://buddyclient.vercel.app/auth/reset-password?token=");
    }
}
