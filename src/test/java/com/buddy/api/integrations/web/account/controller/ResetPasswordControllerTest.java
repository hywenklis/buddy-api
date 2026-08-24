package com.buddy.api.integrations.web.account.controller;

import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectNotFoundFrom;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.commons.configurations.cache.ForgotPasswordTokenManager;
import com.buddy.api.commons.configurations.security.jwt.TokenBlocklistService;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.accounts.requests.ResetPasswordRequest;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("Reset Password Controller Tests")
@Import(ResetPasswordControllerTest.TokenBlocklistTestConfiguration.class)
@ActiveProfiles("test")
class ResetPasswordControllerTest extends IntegrationTestAbstract {
    private static final String NEW_PASSWORD = "NewPassword123!";

    private static final String RESET_PASSWORD_URL = "/v1/accounts/password/reset";

    @Autowired
    private ForgotPasswordTokenManager tokenManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenBlocklistService tokenBlocklistService;

    private AccountEntity testUser;

    @BeforeEach
    void setup() {
        reset(tokenBlocklistService);
        testUser = AccountBuilder.validAccountEntity().build();
        testUser = accountRepository.save(testUser);
    }

    @Nested
    @DisplayName("POST /v1/accounts/password/reset")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should return 200 OK on valid reset password request")
        void should_reset_password_successfully() throws Exception {
            String token = tokenManager.generateAndStoreToken(testUser.getEmail().value());
            String newPassword = NEW_PASSWORD;

            ResetPasswordRequest request = new ResetPasswordRequest(token, newPassword);

            mockMvc.perform(post(RESET_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            AccountEntity updatedAccount = accountRepository
                .findById(testUser.getAccountId()).orElseThrow();
            assertThat(passwordEncoder.matches(newPassword, updatedAccount.getPassword())).isTrue();
            
            assertThat(tokenManager.getEmailByToken(token)).isNull();
            org.mockito.Mockito.verify(tokenBlocklistService)
                .revokeAllUserTokens(testUser.getEmail().value());
        }

        @Test
        @DisplayName("Should return 404 Not Found when token is invalid")
        void should_return_not_found_when_token_invalid() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest(
                "invalid-token", 
                NEW_PASSWORD
            );

            expectNotFoundFrom(
                mockMvc.perform(post(RESET_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("token", "Invalid or expired reset token");

            org.mockito.Mockito.verify(tokenBlocklistService, org.mockito.Mockito.never())
                .revokeAllUserTokens(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when password is weak")
        void should_return_bad_request_when_password_weak() throws Exception {
            String token = tokenManager.generateAndStoreToken(testUser.getEmail().value());
            String weakPassword = RandomStringUtils.secure().nextAlphabetic(6)
                .toLowerCase(Locale.ROOT);
            ResetPasswordRequest request = new ResetPasswordRequest(token, weakPassword);

            expectBadRequestFrom(
                mockMvc.perform(post(RESET_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("newPassword",
                "New password must contain uppercase, lowercase, number, and special character");
        }
        
        @Test
        @DisplayName("Should return 400 Bad Request when token is blank")
        void should_return_bad_request_when_token_blank() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest("", NEW_PASSWORD);

            expectBadRequestFrom(
                mockMvc.perform(post(RESET_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("token", "Reset token is mandatory");
        }

        @Test
        @DisplayName("Should return 404 on second attempt with same token (single-use)")
        void should_return_not_found_on_second_attempt() throws Exception {
            String token = tokenManager.generateAndStoreToken(testUser.getEmail().value());
            ResetPasswordRequest request = new ResetPasswordRequest(token, NEW_PASSWORD);

            mockMvc.perform(post(RESET_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            expectNotFoundFrom(
                mockMvc.perform(post(RESET_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("token", "Invalid or expired reset token");
        }

        @Test
        @DisplayName("Should allow only one concurrent request to consume a reset token")
        void should_allow_only_one_concurrent_token_consumer() throws Exception {
            String token = tokenManager.generateAndStoreToken(testUser.getEmail().value());
            String newPassword = RandomStringUtils.secure().nextAlphanumeric(10) + "aA1!";
            ResetPasswordRequest request = new ResetPasswordRequest(token, newPassword);
            ExecutorService executor = Executors.newFixedThreadPool(2);
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);

            try {
                Future<Integer> first = submitResetRequest(executor, ready, start, request);
                Future<Integer> second = submitResetRequest(executor, ready, start, request);

                ready.await();
                start.countDown();

                assertThat(List.of(first.get(), second.get()))
                    .containsExactlyInAnyOrder(200, 404);
            } finally {
                executor.shutdownNow();
            }
        }



        private Future<Integer> submitResetRequest(
            final ExecutorService executor,
            final CountDownLatch ready,
            final CountDownLatch start,
            final ResetPasswordRequest request
        ) {
            return executor.submit(() -> {
                ready.countDown();
                start.await();
                return mockMvc.perform(post(RESET_PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andReturn()
                    .getResponse()
                    .getStatus();
            });
        }

    }

    @TestConfiguration
    static class TokenBlocklistTestConfiguration {
        @Bean
        @Primary
        TokenBlocklistService tokenBlocklistService() {
            return mock(TokenBlocklistService.class);
        }

        @Test
        @DisplayName("Should return 404 on second attempt with same token (single-use)")
        void should_return_not_found_on_second_attempt() throws Exception {
            String token = tokenManager.generateAndStoreToken(testUser.getEmail().value());
            ResetPasswordRequest request = new ResetPasswordRequest(token, "NewPassword123!");

            mockMvc.perform(post(RESET_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            expectNotFoundFrom(
                mockMvc.perform(post(RESET_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("token", "Invalid or expired reset token");
        }
    }
}
