package com.buddy.api.commons.configuration.security;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.buddy.api.commons.configuration.jwt.JwtAuthenticationFilter;
import com.buddy.api.domains.authentication.services.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // TODO: criar interface para -> CustomUserDetailsService
    // TODO: Analisar endpoints que ficaram publicos e que vai ser necessário ter autoridades,
    // TODO: Analisar endpoints privados e que vai ser necessário ter autoridades
    // TODO: Extrair AuthenticationManager para uma classe especifica

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v1/auth", "v1/accounts/register").permitAll()
                .requestMatchers("/v1/admin/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            ).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        final AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}
