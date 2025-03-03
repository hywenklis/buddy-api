package com.buddy.api.commons.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
public class AuthenticationManagerProvider {
    @Bean
    public AuthenticationManager authenticationManager(
        final AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}
