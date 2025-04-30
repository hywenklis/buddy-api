package com.buddy.api.commons.configurations.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AccountPasswordEncoderProvider {
    @Bean
    public PasswordEncoder accountPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
