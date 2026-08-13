package com.buddy.api.integrations.web.account.controller;

import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectNotFoundFrom;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.commons.configurations.cache.ForgotPasswordTokenManager;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.accounts.requests.ResetPasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

@DisplayName("Reset Password Controller Tests")
class ResetPasswordControllerTest extends IntegrationTestAbstract {

    private static final String RESET_PASSWORD_URL = "/v1/accounts/password/reset";

    @Autowired
    private ForgotPasswordTokenManager tokenManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AccountEntity testUser;

    @BeforeEach
    void setup() {
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
            String newPassword = "NewPassword123!";

            ResetPasswordRequest request = new ResetPasswordRequest(token, newPassword);

            mockMvc.perform(post(RESET_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            AccountEntity updatedAccount = accountRepository.findById(testUser.getAccountId()).orElseThrow();
            assertThat(passwordEncoder.matches(newPassword, updatedAccount.getPassword())).isTrue();
            
            // token should be removed
            assertThat(tokenManager.getEmailByToken(token)).isNull();
        }

        @Test
        @DisplayName("Should return 404 Not Found when token is invalid")
        void should_return_not_found_when_token_invalid() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest("invalid-token", "NewPassword123!");

            expectNotFoundFrom(
                mockMvc.perform(post(RESET_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("token", "Invalid or expired reset token");
        }

        @Test
        @DisplayName("Should return 400 Bad Request when password is weak")
        void should_return_bad_request_when_password_weak() throws Exception {
            String token = tokenManager.generateAndStoreToken(testUser.getEmail().value());
            ResetPasswordRequest request = new ResetPasswordRequest(token, "weak");

            expectBadRequestFrom(
                mockMvc.perform(post(RESET_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("newPassword", "New password must have between 6 and 16 characters");
        }
        
        @Test
        @DisplayName("Should return 400 Bad Request when token is blank")
        void should_return_bad_request_when_token_blank() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest("", "NewPassword123!");

            expectBadRequestFrom(
                mockMvc.perform(post(RESET_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("token", "Reset token is mandatory");
        }
    }
}
