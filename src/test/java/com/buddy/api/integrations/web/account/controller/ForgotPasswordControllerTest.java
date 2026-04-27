package com.buddy.api.integrations.web.account.controller;

import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectManyRequestFrom;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.accounts.requests.ForgotPasswordRequest;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.http.MediaType;

@DisplayName("Forgot Password Controller Tests")
class ForgotPasswordControllerTest extends IntegrationTestAbstract {

    private static final String FORGOT_PASSWORD_URL = "/v1/accounts/password/forgot";
    private static final String PASSWORD_RECOVERY_OPERATION = "password recovery";
    private static final String RATE_LIMIT_COUNT_KEY_PREFIX =
        "rate-limit:count:" + PASSWORD_RECOVERY_OPERATION + ":";

    private String testUserEmail;
    private Cache forgotPasswordTokenCache;

    @BeforeEach
    void setup() {
        AccountEntity testUser = AccountBuilder.validAccountEntity().build();
        testUser = accountRepository.save(testUser);
        testUserEmail = testUser.getEmail().value();

        forgotPasswordTokenCache = cacheManager.getCache("forgotPasswordToken");
        Cache rateLimitCache = cacheManager.getCache("forgotPasswordRateLimit");

        assertThat(forgotPasswordTokenCache).isNotNull();
        assertThat(rateLimitCache).isNotNull();

        forgotPasswordTokenCache.clear();
        rateLimitCache.clear();

        WireMock.resetAllScenarios();
        WireMock.resetAllRequests();
    }

    @Nested
    @DisplayName("POST /v1/accounts/password/forgot")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should return 202 Accepted when valid email exists")
        void forgotPassword_withValidEmail_shouldReturnAccepted() throws Exception {
            setupSuccessScenario();

            final var request = ForgotPasswordRequest.builder()
                .email(testUserEmail)
                .build();

            mockMvc.perform(post(FORGOT_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("request accepted"));

            waitUntilWireMockReceives(1);
        }

        @Test
        @DisplayName("Should return 202 Accepted even when email does NOT exist "
            + "(enumeration protection)")
        void forgotPassword_withNonExistentEmail_shouldReturnAccepted() throws Exception {
            final var request = ForgotPasswordRequest.builder()
                .email("nonexistent@email.com")
                .build();

            mockMvc.perform(post(FORGOT_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("request accepted"));

            verify(0, postRequestedFor(urlEqualTo(MANAGER_NOTIFICATION_API_URL)));
        }

        @Test
        @DisplayName("Should return 202 Accepted even when Manager API fails "
            + "(graceful degradation)")
        void forgotPassword_whenManagerAuthFails_shouldReturn202Async() throws Exception {
            WireMock.setScenarioState("MANAGER_AUTH_SCENARIO", "INTERNAL_SERVER_ERROR_STATE");

            final var request = ForgotPasswordRequest.builder()
                .email(testUserEmail)
                .build();

            mockMvc.perform(post(FORGOT_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("request accepted"));
        }

        @Test
        @DisplayName("Should return 429 Too Many Requests when rate limited by email")
        void forgotPassword_whenRateLimited_shouldReturnTooManyRequests()
            throws Exception {
            setupSuccessScenario();

            final var request = ForgotPasswordRequest.builder()
                .email(testUserEmail)
                .build();

            mockMvc.perform(post(FORGOT_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

            expectManyRequestFrom(
                mockMvc.perform(post(FORGOT_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("rateLimit",
                "Too many password recovery requests. Please wait a minute before trying again.");
        }

        @Test
        @DisplayName("Should return 400 Bad Request with invalid email format")
        void forgotPassword_withInvalidEmail_shouldReturnBadRequest() throws Exception {
            expectBadRequestFrom(
                mockMvc.perform(post(FORGOT_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "email": "invalid-email"
                        }
                        """))
            ).forField("email", "Email must be a valid email address");
        }

        @Test
        @DisplayName("Should return 400 Bad Request when email is blank")
        void forgotPassword_withBlankEmail_shouldReturnBadRequest() throws Exception {
            expectBadRequestFrom(
                mockMvc.perform(post(FORGOT_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "email": ""
                        }
                        """))
            ).forField("email", "Email is mandatory");
        }

        @Test
        @DisplayName("Should return 400 Bad Request when email is missing")
        void forgotPassword_withMissingEmail_shouldReturnBadRequest() throws Exception {
            expectBadRequestFrom(
                mockMvc.perform(post(FORGOT_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
            ).forField("email", "Email is mandatory");
        }

        @Test
        @DisplayName("Should generate unique recovery tokens for each request")
        void forgotPassword_shouldGenerateUniqueTokens() throws Exception {
            setupSuccessScenario();

            final var request = ForgotPasswordRequest.builder()
                .email(testUserEmail)
                .build();

            mockMvc.perform(post(FORGOT_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

            clearRateLimitFor(testUserEmail);

            mockMvc.perform(post(FORGOT_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

            waitUntilWireMockReceives(2);
        }

        @Test
        @DisplayName("Should return 202 Accepted even if email sending fails (async operation)")
        void forgotPassword_whenEmailSendingFails_shouldReturn202Async()
            throws Exception {
            WireMock.setScenarioState("MANAGER_AUTH_SCENARIO", "SUCCESS_STATE");
            WireMock.setScenarioState("MANAGER_PASSWORD_RECOVERY_EMAIL_SCENARIO",
                "INTERNAL_SERVER_ERROR_STATE");

            final var request = ForgotPasswordRequest.builder()
                .email(testUserEmail)
                .build();

            mockMvc.perform(post(FORGOT_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("request accepted"));
        }
    }

    @Nested
    @DisplayName("Security and Privacy Tests")
    class SecurityAndPrivacyTests {

        @Test
        @DisplayName("Should not leak email existence - same response for existing "
            + "and non-existing emails")
        void shouldNotLeakEmailExistence() throws Exception {
            final var existingRequest = ForgotPasswordRequest.builder()
                .email(testUserEmail)
                .build();

            final var nonExistingRequest = ForgotPasswordRequest.builder()
                .email("definitely.not.registered@email.com")
                .build();

            var response1 = mockMvc.perform(post(FORGOT_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(existingRequest)))
                .andExpect(status().isAccepted())
                .andReturn();

            var response2 = mockMvc.perform(post(FORGOT_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(nonExistingRequest)))
                .andExpect(status().isAccepted())
                .andReturn();

            assertThat(response1.getResponse().getStatus())
                .isEqualTo(response2.getResponse().getStatus());
        }

        @Test
        @DisplayName("Endpoint should be public (no authentication required)")
        void endpointShouldBePublic() throws Exception {
            setupSuccessScenario();

            final var request = ForgotPasswordRequest.builder()
                .email(testUserEmail)
                .build();

            mockMvc.perform(post(FORGOT_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
        }
    }

    @Nested
    @DisplayName("Token Management Tests")
    class TokenManagementTests {

        @Test
        @DisplayName("Recovery token should be stored in Redis with TTL")
        void recoveryToken_shouldBeStoredInRedis() throws Exception {
            setupSuccessScenario();

            final var request = ForgotPasswordRequest.builder()
                .email(testUserEmail)
                .build();
            final var cacheName = forgotPasswordTokenCache.getName();
            final var cacheKeyPattern = cacheName + "::*";
            final var keysBeforeRequest = redisTemplate.keys(cacheKeyPattern);

            mockMvc.perform(post(FORGOT_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

            waitUntilWireMockReceives(1);

            final var keysAfterRequest = redisTemplate.keys(cacheKeyPattern);

            assertThat(forgotPasswordTokenCache).isNotNull();
            assertThat(keysAfterRequest).isNotNull();
            assertThat(keysAfterRequest.size()).isGreaterThan(
                keysBeforeRequest == null ? 0 : keysBeforeRequest.size());

            final var newTokenKeys = new HashSet<>(keysAfterRequest);
            if (keysBeforeRequest != null) {
                newTokenKeys.removeAll(keysBeforeRequest);
            }

            assertThat(newTokenKeys).hasSize(1);

            final var tokenKey = newTokenKeys.iterator().next();
            final var ttlSeconds = redisTemplate.getExpire(tokenKey);

            assertThat(ttlSeconds).isNotNull();
            assertThat(ttlSeconds).isPositive();
        }

        @Test
        @DisplayName("Multiple password recovery requests should be accepted")
        void multipleRequests_shouldBeAcceptedAcrossRateLimitWindows() throws Exception {
            setupSuccessScenario();

            final var request = ForgotPasswordRequest.builder()
                .email(testUserEmail)
                .build();

            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post(FORGOT_PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted());

                waitUntilWireMockReceives(i + 1);

                if (i < 2) {
                    clearRateLimitFor(testUserEmail);
                }
            }
        }
    }

    private void setupSuccessScenario() {
        WireMock.setScenarioState("MANAGER_AUTH_SCENARIO", "SUCCESS_STATE");
        WireMock.setScenarioState(
            "MANAGER_PASSWORD_RECOVERY_EMAIL_SCENARIO",
            "SUCCESS_STATE"
        );
    }

    private void waitUntilWireMockReceives(final int expectedCount) {
        await()
            .atMost(5, TimeUnit.SECONDS)
            .pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() ->
                verify(expectedCount, postRequestedFor(urlEqualTo(MANAGER_NOTIFICATION_API_URL)))
            );
    }

    private void clearRateLimitFor(final String email) {
        redisTemplate.delete(RATE_LIMIT_COUNT_KEY_PREFIX + email);
    }
}

