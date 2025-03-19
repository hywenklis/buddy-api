package com.buddy.api.integrations.web.auth.controller;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static com.buddy.api.utils.RandomEmailUtils.generateValidEmail;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.builders.profile.ProfileBuilder;
import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.authetication.requests.AuthRequest;
import jakarta.servlet.http.Cookie;
import java.util.Objects;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

class AuthControllerTest extends IntegrationTestAbstract {

    private static final String WEB_ORIGIN = "550e8400-e29b-41d4-a716-446655440000";

    @Test
    @DisplayName("Should authenticate user successfully")
    void authenticate_user_success() throws Exception {
        var plainPassword = RandomStringUtils.secure().nextAlphanumeric(10);
        var accountEntity = validAccountEntity()
            .password(passwordEncoder.encode(plainPassword))
            .build();
        accountRepository.save(accountEntity);

        var profileEntity = ProfileBuilder.profileEntity()
            .account(accountEntity)
            .build();
        profileRepository.save(profileEntity);

        var authRequest = AuthRequest.builder()
            .email(accountEntity.getEmail().value())
            .password(plainPassword)
            .build();

        ResultActions result = performAuthRequest(authRequest);

        result.andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "application/json"))
            .andExpect(header().string("Access-Control-Allow-Origin", WEB_ORIGIN))
            .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-XSS-Protection", "0"))
            .andExpect(
                header().string("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"))
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
            .andExpect(jsonPath("$.profiles[0].name", equalTo(profileEntity.getName())))
            .andExpect(
                jsonPath("$.profiles[0].profileType",
                    equalTo(profileEntity.getProfileType().name()))
            );
    }

    @Test
    @DisplayName("Should not authenticate if email is not provided")
    void should_not_authenticate_without_email() throws Exception {
        var authRequest = AuthRequest.builder()
            .email(null)
            .password(RandomStringUtils.secure().nextAlphanumeric(10))
            .build();

        expectBadRequestFrom(performAuthRequest(authRequest))
            .forField(EMAIL, "Account email is mandatory");
    }

    @Test
    @DisplayName("Should not authenticate if password is not provided")
    void should_not_authenticate_without_password() throws Exception {
        var authRequest = AuthRequest.builder()
            .email(generateValidEmail())
            .password(null)
            .build();

        expectBadRequestFrom(performAuthRequest(authRequest))
            .forField("password", "Account password is mandatory");
    }

    @Test
    @DisplayName("Should not authenticate if credentials are invalid")
    void should_not_authenticate_with_invalid_credentials() throws Exception {
        var accountEntity = validAccountEntity().build();
        accountRepository.save(accountEntity);

        var authRequest = AuthRequest.builder()
            .email(accountEntity.getEmail().value())
            .password(RandomStringUtils.secure().nextAlphanumeric(10))
            .build();

        ResultActions result = performAuthRequest(authRequest);

        result.andExpect(status().isUnauthorized())
            .andExpect(jsonPath(ERROR_FIELD_NAME, equalTo("credentials")))
            .andExpect(jsonPath(ERROR_MESSAGE_NAME,
                equalTo("Authentication error occurred: Bad credentials")))
            .andExpect(jsonPath(
                ERROR_HTTPSTATUS_NAME, equalTo(UNAUTHORIZED_NAME))
            )
            .andExpect(jsonPath(ERROR_CODE_NAME, equalTo(401)))
            .andExpect(jsonPath(ERROR_TIMESTAMP_NAME).exists());
    }

    @Test
    @DisplayName("Should not authenticate if account is blocked")
    void should_not_authenticate_if_account_blocked() throws Exception {
        var plainPassword = RandomStringUtils.secure().nextAlphanumeric(10);
        var accountEntity = validAccountEntity()
            .password(passwordEncoder.encode(plainPassword))
            .isBlocked(true)
            .build();
        accountRepository.save(accountEntity);

        var authRequest = AuthRequest.builder()
            .email(accountEntity.getEmail().value())
            .password(plainPassword)
            .build();

        ResultActions result = performAuthRequest(authRequest);

        result.andExpect(status().isUnauthorized())
            .andExpect(jsonPath(ERROR_FIELD_NAME, equalTo("credentials")))
            .andExpect(jsonPath(ERROR_MESSAGE_NAME,
                equalTo("Authentication error occurred: Account is not available")))
            .andExpect(jsonPath(ERROR_HTTPSTATUS_NAME, equalTo(UNAUTHORIZED_NAME)))
            .andExpect(jsonPath(ERROR_CODE_NAME, equalTo(401)))
            .andExpect(jsonPath(ERROR_TIMESTAMP_NAME).exists());
    }

    @Test
    @DisplayName("Should not authenticate if account is deleted")
    void should_not_authenticate_if_account_deleted() throws Exception {
        var plainPassword = RandomStringUtils.secure().nextAlphanumeric(10);
        var accountEntity = validAccountEntity()
            .password(passwordEncoder.encode(plainPassword))
            .isDeleted(true)
            .build();
        accountRepository.save(accountEntity);

        var authRequest = AuthRequest.builder()
            .email(accountEntity.getEmail().value())
            .password(plainPassword)
            .build();

        ResultActions result = performAuthRequest(authRequest);

        result.andExpect(status().isUnauthorized())
            .andExpect(jsonPath(ERROR_FIELD_NAME, equalTo("credentials")))
            .andExpect(jsonPath(ERROR_MESSAGE_NAME,
                equalTo("Authentication error occurred: Account is not available")))
            .andExpect(jsonPath(ERROR_HTTPSTATUS_NAME, equalTo(UNAUTHORIZED_NAME)))
            .andExpect(jsonPath(ERROR_CODE_NAME, equalTo(401)))
            .andExpect(jsonPath(ERROR_TIMESTAMP_NAME).exists());
    }

    @Test
    @DisplayName("Should not authenticate if email format is invalid")
    void should_not_authenticate_with_invalid_email_format() throws Exception {
        var authRequest = AuthRequest.builder()
            .email(RandomStringUtils.secure().nextAlphabetic(10))
            .password(RandomStringUtils.secure().nextAlphanumeric(10))
            .build();

        expectBadRequestFrom(performAuthRequest(authRequest))
            .forField(EMAIL, "Account email must be a valid email address");
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void refresh_token_success() throws Exception {
        var plainPassword = RandomStringUtils.secure().nextAlphanumeric(10);
        var accountEntity = validAccountEntity()
            .password(passwordEncoder.encode(plainPassword))
            .build();
        accountRepository.save(accountEntity);

        var authRequest = AuthRequest.builder()
            .email(accountEntity.getEmail().value())
            .password(plainPassword)
            .build();

        ResultActions authResult = performAuthRequest(authRequest);
        String refreshToken = Objects.requireNonNull(authResult
                .andReturn()
                .getResponse()
                .getCookie(REFRESH_TOKEN_NAME))
            .getValue();

        ResultActions result = mockMvc.perform(post(REFRESH_URL)
            .header(ORIGIN, WEB_ORIGIN)
            .contentType(APPLICATION_JSON)
            .cookie(new Cookie(REFRESH_TOKEN_NAME, refreshToken)));

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

    @Test
    @DisplayName("Should not refresh token if refresh token is missing")
    void should_not_refresh_without_token() throws Exception {
        ResultActions result = mockMvc.perform(post(REFRESH_URL)
            .header(ORIGIN, WEB_ORIGIN)
            .contentType(APPLICATION_JSON));

        result.andExpect(status().isUnauthorized())
            .andExpect(jsonPath(ERROR_FIELD_NAME, equalTo("refresh-token")))
            .andExpect(jsonPath(ERROR_MESSAGE_NAME, equalTo("Refresh token is required")))
            .andExpect(jsonPath(ERROR_HTTPSTATUS_NAME, equalTo(UNAUTHORIZED_NAME)))
            .andExpect(jsonPath(ERROR_CODE_NAME, equalTo(401)))
            .andExpect(jsonPath(ERROR_TIMESTAMP_NAME).exists());
    }

    @Test
    @DisplayName("Should not refresh token if account is blocked")
    void should_not_refresh_if_account_blocked() throws Exception {
        var plainPassword = RandomStringUtils.secure().nextAlphanumeric(10);
        var accountEntity = validAccountEntity()
            .password(passwordEncoder.encode(plainPassword))
            .build();
        accountRepository.save(accountEntity);

        var authRequest = AuthRequest.builder()
            .email(accountEntity.getEmail().value())
            .password(plainPassword)
            .build();

        ResultActions authResult = performAuthRequest(authRequest);
        String refreshToken = Objects.requireNonNull(authResult
                .andReturn()
                .getResponse()
                .getCookie(REFRESH_TOKEN_NAME))
            .getValue();

        accountEntity.setIsBlocked(true);
        accountRepository.save(accountEntity);

        ResultActions result = mockMvc.perform(post(REFRESH_URL)
            .header(ORIGIN, WEB_ORIGIN)
            .contentType(APPLICATION_JSON)
            .cookie(new Cookie(REFRESH_TOKEN_NAME, refreshToken)));

        result.andExpect(status().isForbidden())
            .andExpect(jsonPath(ERROR_FIELD_NAME, equalTo(EMAIL)))
            .andExpect(jsonPath(ERROR_MESSAGE_NAME, equalTo("Account is not available")))
            .andExpect(jsonPath(ERROR_HTTPSTATUS_NAME, equalTo("FORBIDDEN")))
            .andExpect(jsonPath(ERROR_CODE_NAME, equalTo(403)))
            .andExpect(jsonPath(ERROR_TIMESTAMP_NAME).exists());
    }

    @Test
    @DisplayName("Should not refresh token if account is deleted")
    void should_not_refresh_if_account_deleted() throws Exception {
        var plainPassword = RandomStringUtils.secure().nextAlphanumeric(10);
        var accountEntity = validAccountEntity()
            .password(passwordEncoder.encode(plainPassword))
            .build();
        accountRepository.save(accountEntity);

        var authRequest = AuthRequest.builder()
            .email(accountEntity.getEmail().value())
            .password(plainPassword)
            .build();

        ResultActions authResult = performAuthRequest(authRequest);
        String refreshToken = Objects.requireNonNull(authResult
                .andReturn()
                .getResponse()
                .getCookie(REFRESH_TOKEN_NAME))
            .getValue();

        accountEntity.setIsDeleted(true);
        accountRepository.save(accountEntity);

        ResultActions result = mockMvc.perform(post(REFRESH_URL)
            .header(ORIGIN, WEB_ORIGIN)
            .contentType(APPLICATION_JSON)
            .cookie(new Cookie(REFRESH_TOKEN_NAME, refreshToken)));

        result.andExpect(status().isForbidden())
            .andExpect(jsonPath(ERROR_FIELD_NAME, equalTo(EMAIL)))
            .andExpect(jsonPath(ERROR_MESSAGE_NAME, equalTo("Account is not available")))
            .andExpect(jsonPath(ERROR_HTTPSTATUS_NAME, equalTo("FORBIDDEN")))
            .andExpect(jsonPath(ERROR_CODE_NAME, equalTo(403)))
            .andExpect(jsonPath(ERROR_TIMESTAMP_NAME).exists());
    }

    private ResultActions performAuthRequest(final AuthRequest request) throws Exception {
        return mockMvc.perform(post(AUTH_URL)
            .header(ORIGIN, WEB_ORIGIN)
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));
    }
}
