package com.buddy.api.integrations.web.email.controller;

import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectManyRequestFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectNotFoundFrom;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.commons.configurations.properties.RateLimitProperties;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import com.buddy.api.web.accounts.requests.ConfirmEmailRequest;
import com.buddy.api.web.accounts.requests.RequestEmailVerificationRequest;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.http.MediaType;

@DisplayName("Email Verification Controller Tests")
class EmailVerificationControllerTest extends IntegrationTestAbstract {

    @Autowired
    private RateLimitProperties rateLimitProperties;

    private AccountEntity testUser;
    private Cache verificationTokenCache;

    @BeforeEach
    void setup() {
        testUser = accountRepository.save(
            AccountBuilder.validAccountEntity().isVerified(false).build()
        );

        verificationTokenCache = cacheManager.getCache("emailVerificationToken");
        Cache rateLimitCache = cacheManager.getCache("emailVerificationRateLimit");
        assertThat(verificationTokenCache).isNotNull();
        assertThat(rateLimitCache).isNotNull();
        WireMock.resetAllScenarios();
        WireMock.resetAllRequests();
    }

    @Nested
    @DisplayName("POST /v1/accounts/verifications/request")
    class RequestVerificationTests {

        @Test
        @DisplayName("Should return 202 Accepted and send email for unverified account")
        void requestVerification_happyPath_shouldReturnAccepted() throws Exception {
            setupSuccessScenario();
            var request = RequestEmailVerificationRequest.builder()
                .email(testUser.getEmail().value())
                .build();

            mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

            waitUntilWireMockReceives(1);
        }

        @Test
        @DisplayName("Should return 202 without sending email when account is already verified")
        void requestVerification_whenAccountAlreadyVerified_shouldReturnAcceptedSilently()
            throws Exception {
            testUser.setIsVerified(true);
            accountRepository.save(testUser);

            var request = RequestEmailVerificationRequest.builder()
                .email(testUser.getEmail().value())
                .build();

            mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

            verify(0, postRequestedFor(urlEqualTo(MANAGER_API_URL)));
            verify(0, postRequestedFor(urlEqualTo(MANAGER_NOTIFICATION_API_URL)));
        }

        @Test
        @DisplayName("Should return 202 without sending email when email does not exist")
        void requestVerification_whenEmailNotFound_shouldReturnAcceptedSilently()
            throws Exception {
            var request = RequestEmailVerificationRequest.builder()
                .email(RandomEmailUtils.generateValidEmail())
                .build();

            mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

            verify(0, postRequestedFor(urlEqualTo(MANAGER_API_URL)));
            verify(0, postRequestedFor(urlEqualTo(MANAGER_NOTIFICATION_API_URL)));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when email is invalid")
        void requestVerification_whenEmailInvalid_shouldReturnBadRequest() throws Exception {
            var request = RequestEmailVerificationRequest.builder()
                .email("invalid-email")
                .build();

            expectBadRequestFrom(
                mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("email", "Account email must be a valid email address");
        }

        @Test
        @DisplayName("Should return 429 Too Many Requests when rate limited")
        void requestVerification_whenRateLimited_shouldReturnTooManyRequests() throws Exception {
            setupSuccessScenario();
            var request = RequestEmailVerificationRequest.builder()
                .email(testUser.getEmail().value())
                .build();

            int maxAttempts = rateLimitProperties.maxAttempts();
            for (int i = 0; i < maxAttempts; i++) {
                mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted());
            }

            expectManyRequestFrom(
                mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("rateLimit",
                "Too many verification requests. Please wait a minute before trying again.");
        }
    }

    @Nested
    @DisplayName("POST /v1/accounts/verifications/confirm")
    class ConfirmVerificationTests {

        @Test
        @DisplayName("Should confirm email publicly and consume token on valid request")
        void confirm_withValidToken_shouldUpdateAccountAndReturnOk() throws Exception {
            final String tokenValue = UUID.randomUUID().toString();
            verificationTokenCache.put(tokenValue, testUser.getEmail().value());

            final var request = ConfirmEmailRequest.builder().token(tokenValue).build();

            mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_CONFIRM)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

            final AccountEntity updatedUser =
                accountRepository.findById(testUser.getAccountId()).orElseThrow();
            assertThat(updatedUser.getIsVerified()).isTrue();
            assertThat(verificationTokenCache.get(tokenValue)).isNull();
        }

        @Test
        @DisplayName("Should return 200 OK idempotently if account is already verified")
        void confirm_whenAccountIsAlreadyVerified_shouldReturnOk() throws Exception {
            testUser.setIsVerified(true);
            accountRepository.save(testUser);

            final String tokenValue = UUID.randomUUID().toString();
            verificationTokenCache.put(tokenValue, testUser.getEmail().value());

            final var request = ConfirmEmailRequest.builder().token(tokenValue).build();

            mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_CONFIRM)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

            assertThat(verificationTokenCache.get(tokenValue)).isNull();
        }

        @Test
        @DisplayName("Should return 404 Not Found when token does not exist or is expired")
        void confirm_withUnknownToken_shouldReturnNotFound() throws Exception {
            final var request = ConfirmEmailRequest.builder()
                .token(UUID.randomUUID().toString())
                .build();

            expectNotFoundFrom(
                mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_CONFIRM)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("token", "Invalid or expired verification token.");
        }

        @Test
        @DisplayName("Should return 404 Not Found when reusing an already consumed token")
        void confirm_whenReusingConsumedToken_shouldReturnNotFound() throws Exception {
            final String tokenValue = UUID.randomUUID().toString();
            verificationTokenCache.put(tokenValue, testUser.getEmail().value());
            final var request = ConfirmEmailRequest.builder().token(tokenValue).build();

            mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_CONFIRM)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            expectNotFoundFrom(
                mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_CONFIRM)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("token", "Invalid or expired verification token.");
        }

        @Test
        @DisplayName("Should allow only one concurrent request to consume a verification token")
        void confirm_concurrentRequests_shouldAllowOnlyOne() throws Exception {
            final String tokenValue = UUID.randomUUID().toString();
            verificationTokenCache.put(tokenValue, testUser.getEmail().value());
            final var request = ConfirmEmailRequest.builder().token(tokenValue).build();

            ExecutorService executor = Executors.newFixedThreadPool(2);
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);

            try {
                Future<Integer> first = submitConfirmRequest(executor, ready, start, request);
                Future<Integer> second = submitConfirmRequest(executor, ready, start, request);

                ready.await();
                start.countDown();

                assertThat(List.of(first.get(), second.get()))
                    .containsExactlyInAnyOrder(200, 404);
            } finally {
                executor.shutdownNow();
            }
        }

        @Test
        @DisplayName("Should return 400 Bad Request if token is missing")
        void confirm_withMissingToken_shouldReturnBadRequest() throws Exception {
            final var request = ConfirmEmailRequest.builder().token(null).build();

            expectBadRequestFrom(
                mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_CONFIRM)
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
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))))
                .forField("token", "token must be a valid UUID format");
        }

        @Test
        @DisplayName("Should return 429 Too Many Requests when confirm is rate limited")
        void confirm_whenRateLimited_shouldReturnTooManyRequests() throws Exception {
            final var request = ConfirmEmailRequest.builder()
                .token(UUID.randomUUID().toString())
                .build();

            int maxAttempts = rateLimitProperties.maxAttempts();
            for (int i = 0; i < maxAttempts; i++) {
                mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_CONFIRM)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
            }

            expectManyRequestFrom(
                mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_CONFIRM)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("rateLimit",
                "Too many verification confirmation attempts. "
                    + "Please wait a minute before trying again.");
        }

        private Future<Integer> submitConfirmRequest(
            final ExecutorService executor,
            final CountDownLatch ready,
            final CountDownLatch start,
            final ConfirmEmailRequest request
        ) {
            return executor.submit(() -> {
                ready.countDown();
                start.await();
                return mockMvc.perform(post(VERIFICATION_URL + PATH_EMAIL_VERIFICATION_CONFIRM)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andReturn()
                    .getResponse()
                    .getStatus();
            });
        }
    }

    private void setupSuccessScenario() {
        WireMock.setScenarioState("MANAGER_AUTH_SCENARIO", "SUCCESS_STATE");
        WireMock.setScenarioState("MANAGER_NOTIFICATION_EMAIL_SCENARIO", "SUCCESS_STATE");
    }

    private void waitUntilWireMockReceives(final int expectedCount) {
        await()
            .atMost(5, TimeUnit.SECONDS)
            .pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() ->
                verify(expectedCount, postRequestedFor(urlEqualTo(MANAGER_NOTIFICATION_API_URL)))
            );
    }
}
