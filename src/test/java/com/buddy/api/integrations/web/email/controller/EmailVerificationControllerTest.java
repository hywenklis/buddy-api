package com.buddy.api.integrations.web.email.controller;

import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectConflictFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectErrorStatusFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectInternalServerErrorFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectManyRequestFrom;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.accounts.requests.ConfirmEmailRequest;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@DisplayName("Email Verification Controller Tests")
class EmailVerificationControllerTest extends IntegrationTestAbstract {

    private AccountEntity testUser;
    private String userJwt;
    private Cache verificationTokenCache;
    private Cache rateLimitCache;


    @BeforeEach
    void setup() {
        final var authenticatedUser = accountComponent.createAndAuthenticateUser();
        testUser = authenticatedUser.account();
        userJwt = authenticatedUser.jwt();

        verificationTokenCache = cacheManager.getCache("emailVerificationToken");
        rateLimitCache = cacheManager.getCache("emailVerificationRateLimit");
        assertThat(verificationTokenCache).isNotNull();
        assertThat(rateLimitCache).isNotNull();
        WireMock.resetAllScenarios();
        WireMock.resetAllRequests();
    }

    @Nested
    @DisplayName("POST /v1/accounts/verifications/request")
    class RequestVerificationTests {

        @Test
        @DisplayName("Should return 200 OK and send email on valid request")
        void requestVerification_happyPath_shouldReturnOk() throws Exception {
            setupSuccessScenario();

            mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_REQUEST)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + userJwt))
                .andExpect(status().isOk());

            verify(1, postRequestedFor(urlEqualTo(MANAGER_API_URL)));
            verify(1, postRequestedFor(urlEqualTo(MANAGER_NOTIFICATION_API_URL)));

            String token = getTokenFromCacheByEmail(testUser.getEmail().value());
            assertThat(token).isNotNull();
            assertThat(rateLimitCache.get(testUser.getEmail().value())).isNotNull();
        }

        @Test
        @DisplayName("Should return 409 Conflict if account is already verified")
        void requestVerification_whenAccountAlreadyVerified_shouldReturnConflict()
            throws Exception {
            testUser.setIsVerified(true);
            accountRepository.save(testUser);

            expectConflictFrom(
                mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_REQUEST)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + userJwt))
            ).forField("account.status", "This account is already verified.");

            verify(0, postRequestedFor(urlEqualTo(MANAGER_API_URL)));
            verify(0, postRequestedFor(urlEqualTo(MANAGER_NOTIFICATION_API_URL)));
            assertThat(verificationTokenCache.get(testUser.getEmail().value())).isNull();
        }

        @Test
        @DisplayName("Should return 429 Too Many Requests when rate limited")
        void requestVerification_whenRateLimited_shouldReturnTooManyRequests() throws Exception {
            setupSuccessScenario();

            mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_REQUEST)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + userJwt))
                .andExpect(status().isOk());

            expectManyRequestFrom(
                mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_REQUEST)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + userJwt))
            ).forField("rateLimit",
                "You have requested a verification email recently. "
                    + "Please wait a minute before trying again.");

            verify(1, postRequestedFor(urlEqualTo(MANAGER_API_URL)));
            verify(1, postRequestedFor(urlEqualTo(MANAGER_NOTIFICATION_API_URL)));
        }

        @Test
        @DisplayName("Should return 500 if Manager API authentication fails")
        void requestVerification_whenManagerAuthFails_shouldReturn500() throws Exception {
            WireMock.setScenarioState("MANAGER_AUTH_SCENARIO", "INTERNAL_SERVER_ERROR_STATE");

            expectInternalServerErrorFrom(
                mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_REQUEST)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + userJwt))
            ).forField("integration.error",
                "Error in external service: manager-api");

            verify(1, postRequestedFor(urlEqualTo(MANAGER_API_URL)));
            verify(0, postRequestedFor(urlEqualTo(MANAGER_NOTIFICATION_API_URL)));
            assertThat(verificationTokenCache.get(testUser.getEmail().value())).isNull();
        }

        @Test
        @DisplayName("Should return 500 if Manager API notification fails")
        void requestVerification_whenManagerApiNotificationFails_shouldReturn500()
            throws Exception {

            WireMock.setScenarioState("MANAGER_AUTH_SCENARIO", "SUCCESS_STATE");
            WireMock.setScenarioState(
                "MANAGER_NOTIFICATION_EMAIL_SCENARIO",
                "INTERNAL_SERVER_ERROR_STATE"
            );

            expectInternalServerErrorFrom(
                mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_REQUEST)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + userJwt))
            ).forField("integration.error",
                "Error in external service: manager-api");

            verify(1, postRequestedFor(urlEqualTo(MANAGER_API_URL)));
            verify(1, postRequestedFor(urlEqualTo(MANAGER_NOTIFICATION_API_URL)));
        }
    }

    @Nested
    @DisplayName("POST /v1/accounts/verifications/confirm")
    class ConfirmVerificationTests {

        @Test
        @DisplayName("Should confirm email and return 200 OK with a valid token")
        void confirm_withValidToken_shouldUpdateAccountAndReturnOk() throws Exception {
            final String tokenValue = UUID.randomUUID().toString();
            verificationTokenCache.put(tokenValue, testUser.getEmail().value());

            final var request = ConfirmEmailRequest.builder().token(tokenValue).build();

            mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_CONFIRM)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + userJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

            final AccountEntity updatedUser =
                accountRepository.findById(testUser.getAccountId()).orElseThrow();
            assertThat(updatedUser.getIsVerified()).isTrue();
            assertThat(verificationTokenCache.get(tokenValue)
            ).isNull();
        }

        @Test
        @DisplayName("Should return 200 OK if account is already verified")
        void confirm_whenAccountIsAlreadyVerified_shouldReturnOkAndDoNothing() throws Exception {
            testUser.setIsVerified(true);
            accountRepository.save(testUser);

            final String tokenValue = UUID.randomUUID().toString();
            verificationTokenCache.put(tokenValue, testUser.getEmail().value());

            final var request = ConfirmEmailRequest.builder().token(tokenValue).build();

            mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_CONFIRM)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + userJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

            final AccountEntity updatedUser =
                accountRepository.findById(testUser.getAccountId()).orElseThrow();
            assertThat(updatedUser.getIsVerified()).isTrue();
            assertThat(verificationTokenCache.get(tokenValue)
            ).isNull();
        }

        @Test
        @DisplayName("Should return 401 Unauthorized if token belongs to another user")
        void confirm_withMismatchedToken_shouldReturnUnauthorized() throws Exception {
            final String tokenValue = UUID.randomUUID().toString();
            verificationTokenCache.put(tokenValue, "another.user@example.com");

            final var request = ConfirmEmailRequest.builder().token(tokenValue).build();

            expectErrorStatusFrom(
                mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_CONFIRM)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + userJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))), HttpStatus.UNAUTHORIZED)
                .forField("token", "Token does not belong to the authenticated user");
        }

        @Test
        @DisplayName("Should return 400 Bad Request if token is missing")
        void confirm_withMissingToken_shouldReturnBadRequest() throws Exception {
            final var request = ConfirmEmailRequest.builder().token(null).build();

            expectBadRequestFrom(
                mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_CONFIRM)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + userJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))))
                .forField("token", "token is required");
        }

        @Test
        @DisplayName("Should return 400 Bad Request if token has invalid size")
        void confirm_withInvalidTokenSize_shouldReturnBadRequest() throws Exception {
            final var request = ConfirmEmailRequest.builder().token("invalid-token-size-").build();

            expectBadRequestFrom(
                mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_CONFIRM)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + userJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))))
                .forField("token", "token must be 36 characters long");
        }
    }

    private void setupSuccessScenario() {
        WireMock.setScenarioState("MANAGER_AUTH_SCENARIO", "SUCCESS_STATE");
        WireMock.setScenarioState("MANAGER_NOTIFICATION_EMAIL_SCENARIO", "SUCCESS_STATE");
    }

    private String getTokenFromCacheByEmail(final String email) {
        final String keyPattern = "emailVerificationToken::*";
        Set<String> keys = redisTemplate.keys(keyPattern);

        for (String key : keys) {
            String value = redisTemplate.opsForValue().get(key);
            if (email.equals(value)) {
                return key.replace("emailVerificationToken::", "");
            }
        }
        return null;
    }
}
