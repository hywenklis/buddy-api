package com.buddy.api.integrations.web.authentication.controller;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectErrorStatusFrom;
import static com.buddy.api.domains.profile.enums.ProfileTypeEnum.ADMIN;
import static com.buddy.api.utils.RandomEmailUtils.generateValidEmail;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.builders.profile.ProfileBuilder;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.authentication.requests.AuthRequest;
import jakarta.servlet.http.Cookie;
import java.util.Objects;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

class AuthControllerTest extends IntegrationTestAbstract {

    private static final String WEB_ORIGIN = "550e8400-e29b-41d4-a716-446655440000";

    @Test
    @DisplayName("Should authenticate user successfully and filter out ADMIN profiles")
    void authenticate_user_success() throws Exception {
        String plainPassword = RandomStringUtils.secure().nextAlphanumeric(10);
        var account = validAccountEntity()
            .password(passwordEncoder.encode(plainPassword))
            .build();
        accountRepository.save(account);

        var userProfile = ProfileBuilder.profileEntity()
            .account(account)
            .build();
        var adminProfile = ProfileBuilder.profileEntity()
            .account(account)
            .profileType(ADMIN)
            .build();
        profileRepository.save(userProfile);
        profileRepository.save(adminProfile);

        var req = AuthRequest.builder()
            .email(account.getEmail().value())
            .password(plainPassword)
            .build();
        ResultActions result = performAuthRequest(req);

        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profiles.length()", equalTo(1)))
            .andExpect(
                jsonPath(
                    "$.profiles[0].profileType",
                    equalTo(userProfile.getProfileType().name()))
            )
            .andExpect(jsonPath("$.profiles[?(@.profileType=='ADMIN')]").doesNotExist());

        assertAuthSuccess(result, userProfile);
    }


    @Test
    @DisplayName("Should refresh token successfully")
    void refresh_token_success() throws Exception {
        var plain = RandomStringUtils.secure().nextAlphanumeric(10);
        var account = validAccountEntity()
            .password(passwordEncoder.encode(plain))
            .build();
        accountRepository.save(account);

        String refresh = obtainRefreshToken(plain, account);
        ResultActions result = performRefreshRequest(refresh);

        assertRefreshSuccess(result, refresh);
    }

    @Test
    @DisplayName("Should not authenticate if email is not provided")
    void should_not_authenticate_without_email() throws Exception {
        var req = AuthRequest.builder()
            .email(null)
            .password(RandomStringUtils.secure().nextAlphanumeric(10))
            .build();

        expectBadRequestFrom(performAuthRequest(req))
            .forField("email", "Account email is mandatory");
    }

    @Test
    @DisplayName("Should not authenticate if password is not provided")
    void should_not_authenticate_without_password() throws Exception {
        var req = AuthRequest.builder()
            .email(generateValidEmail())
            .password(null)
            .build();

        expectBadRequestFrom(performAuthRequest(req))
            .forField("password", "Account password is mandatory");
    }

    @Test
    @DisplayName("Should not authenticate with invalid credentials")
    void should_not_authenticate_with_invalid_credentials() throws Exception {
        var account = validAccountEntity().build();
        accountRepository.save(account);
        var req = AuthRequest.builder()
            .email(account.getEmail().value())
            .password(RandomStringUtils.secure().nextAlphanumeric(10))
            .build();

        expectErrorStatusFrom(performAuthRequest(req), UNAUTHORIZED)
            .forField(
                CREDENTIALS_NAME,
                "Authentication error occurred: Bad credentials"
            );
    }

    @Test
    @DisplayName("Should not authenticate if account is not available")
    void should_not_authenticate_if_account_not_available() throws Exception {
        var plain = RandomStringUtils.secure().nextAlphanumeric(10);
        var account = validAccountEntity()
            .password(passwordEncoder.encode(plain))
            .isBlocked(true)
            .isDeleted(false)
            .build();
        accountRepository.save(account);
        var req = AuthRequest.builder()
            .email(account.getEmail().value())
            .password(plain)
            .build();

        expectErrorStatusFrom(performAuthRequest(req), UNAUTHORIZED)
            .forField(
                CREDENTIALS_NAME,
                "Authentication error occurred: Account is not available"
            );

        account.setIsBlocked(false);
        account.setIsDeleted(true);
        accountRepository.save(account);

        expectErrorStatusFrom(performAuthRequest(req), UNAUTHORIZED)
            .forField(
                CREDENTIALS_NAME,
                "Authentication error occurred: Account is not available"
            );
    }

    @Test
    @DisplayName("Should not authenticate with invalid email format")
    void should_not_authenticate_with_invalid_email_format() throws Exception {
        var req = AuthRequest.builder()
            .email(RandomStringUtils.secure().nextAlphanumeric(10))
            .password(RandomStringUtils.secure().nextAlphanumeric(10))
            .build();

        expectBadRequestFrom(performAuthRequest(req))
            .forField("email", "Account email must be a valid email address");
    }

    @Test
    @DisplayName("Should not refresh when refresh token is missing")
    void should_not_refresh_without_token() throws Exception {
        expectErrorStatusFrom(performRefreshRequest(null), UNAUTHORIZED)
            .forField("refresh-token", "Refresh token is required");
    }

    @Test
    @DisplayName("Should not refresh with invalid refresh token")
    void should_not_refresh_with_invalid_token() throws Exception {
        expectErrorStatusFrom(performRefreshRequest("bad.token"), UNAUTHORIZED)
            .forField("refresh-token", "Invalid refresh token or token expired");
    }

    @Test
    @DisplayName("Should forbid refresh if account not available")
    void should_forbid_refresh_if_account_not_available() throws Exception {
        var plain = RandomStringUtils.secure().nextAlphanumeric(10);
        var account = validAccountEntity()
            .password(passwordEncoder.encode(plain))
            .build();
        accountRepository.save(account);
        String refresh = obtainRefreshToken(plain, account);

        account.setIsBlocked(true);
        accountRepository.save(account);
        expectErrorStatusFrom(performRefreshRequest(refresh), FORBIDDEN)
            .forField(CREDENTIALS_NAME, "Account is not available");

        account.setIsBlocked(false);
        account.setIsDeleted(true);
        accountRepository.save(account);
        expectErrorStatusFrom(performRefreshRequest(refresh), FORBIDDEN)
            .forField(CREDENTIALS_NAME, "Account is not available");
    }

    @Test
    @DisplayName("Should not authenticate when email exceeds maximum length")
    void should_not_authenticate_with_email_too_long() throws Exception {
        String longEmail = "a".repeat(101) + "@example.com";
        var req = AuthRequest.builder()
            .email(longEmail)
            .password(RandomStringUtils.secure().nextAlphanumeric(10))
            .build();

        expectBadRequestFrom(performAuthRequest(req))
            .forField("email", "Account email must be a valid email address");
    }

    private ResultActions performAuthRequest(final AuthRequest req) throws Exception {
        return mockMvc.perform(post(AUTH_URL)
            .header(ORIGIN, WEB_ORIGIN)
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)));
    }

    private String obtainRefreshToken(final String plain, final AccountEntity account)
        throws Exception {
        var req = AuthRequest.builder()
            .email(account.getEmail().value())
            .password(plain)
            .build();
        var authResult = performAuthRequest(req)
            .andReturn().getResponse();
        return Objects.requireNonNull(authResult.getCookie(REFRESH_TOKEN_NAME)).getValue();
    }

    private ResultActions performRefreshRequest(final String refreshToken) throws Exception {
        var builder = post(REFRESH_URL)
            .header(ORIGIN, WEB_ORIGIN)
            .contentType(APPLICATION_JSON);
        if (refreshToken != null) {
            builder.cookie(new Cookie(REFRESH_TOKEN_NAME, refreshToken));
        }
        return mockMvc.perform(builder);
    }

    private void assertAuthSuccess(
        final ResultActions result,
        final ProfileEntity profile
    ) throws Exception {
        result.andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "application/json"))
            .andExpect(header().string("Access-Control-Allow-Origin", WEB_ORIGIN))
            .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-XSS-Protection", "0"))
            .andExpect(
                header()
                    .string("Cache-Control",
                        "no-cache, no-store, max-age=0, must-revalidate"
                    )
            )
            .andExpect(header().string("Pragma", "no-cache"))
            .andExpect(header().string("Expires", "0"))
            .andExpect(header().string("X-Frame-Options", "DENY"))
            .andExpect(cookie().exists(ACCESS_TOKEN_NAME))
            .andExpect(cookie().value(ACCESS_TOKEN_NAME, notNullValue()))
            .andExpect(cookie().maxAge(ACCESS_TOKEN_NAME, 900))
            .andExpect(cookie().path(ACCESS_TOKEN_NAME, "/"))
            .andExpect(cookie().secure(ACCESS_TOKEN_NAME, true))
            .andExpect(cookie().httpOnly(ACCESS_TOKEN_NAME, true))
            .andExpect(cookie().exists(REFRESH_TOKEN_NAME))
            .andExpect(cookie().value(REFRESH_TOKEN_NAME, notNullValue()))
            .andExpect(cookie().maxAge(REFRESH_TOKEN_NAME, 86400))
            .andExpect(cookie().path(REFRESH_TOKEN_NAME, "/"))
            .andExpect(cookie().secure(REFRESH_TOKEN_NAME, true))
            .andExpect(cookie().httpOnly(REFRESH_TOKEN_NAME, true))
            .andExpect(jsonPath("$.accessToken", notNullValue()))
            .andExpect(jsonPath("$.refreshToken", notNullValue()))
            .andExpect(jsonPath("$.profiles[0].name", equalTo(profile.getName())))
            .andExpect(
                jsonPath(
                    "$.profiles[0].profileType",
                    equalTo(profile.getProfileType().name()))
            );
    }

    private void assertRefreshSuccess(
        final ResultActions result,
        final String refreshToken
    ) throws Exception {
        result.andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "application/json"))
            .andExpect(header().string("Access-Control-Allow-Origin", WEB_ORIGIN))
            .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
            .andExpect(cookie().exists(ACCESS_TOKEN_NAME))
            .andExpect(cookie().value(ACCESS_TOKEN_NAME, notNullValue()))
            .andExpect(cookie().maxAge(ACCESS_TOKEN_NAME, 900))
            .andExpect(cookie().path(ACCESS_TOKEN_NAME, "/"))
            .andExpect(cookie().secure(ACCESS_TOKEN_NAME, true))
            .andExpect(cookie().httpOnly(ACCESS_TOKEN_NAME, true))
            .andExpect(cookie().exists(REFRESH_TOKEN_NAME))
            .andExpect(cookie().value(REFRESH_TOKEN_NAME, refreshToken))
            .andExpect(jsonPath("$.accessToken", notNullValue()))
            .andExpect(jsonPath("$.refreshToken", equalTo(refreshToken)))
            .andExpect(jsonPath("$.profiles").doesNotExist());
    }
}
