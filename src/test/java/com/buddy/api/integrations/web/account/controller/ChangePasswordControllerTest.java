package com.buddy.api.integrations.web.account.controller;

import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.commons.configurations.security.jwt.TokenBlocklistService;
import com.buddy.api.components.AccountComponent.AuthenticatedTestUser;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.accounts.requests.ChangePasswordRequest;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@DisplayName("Change Password Controller Tests")
class ChangePasswordControllerTest extends IntegrationTestAbstract {

    private static final String CHANGE_PASSWORD_URL = "/v1/accounts/password";

    @Autowired
    private TokenBlocklistService tokenBlocklistService;

    private AuthenticatedTestUser testUser;

    @BeforeEach
    void setup() {
        testUser = accountComponent.createAndAuthenticateUser();
    }

    @Nested
    @DisplayName("PATCH /v1/accounts/password")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should return 200 OK and change password successfully")
        void shouldChangePasswordSuccessfully() throws Exception {
            WireMock.setScenarioState("MANAGER_AUTH_SCENARIO", "SUCCESS_STATE");
            WireMock.setScenarioState("MANAGER_PASSWORD_CHANGED_EMAIL_SCENARIO", "SUCCESS_STATE");

            String newPassword = "NewPassword123!";
            ChangePasswordRequest request = new ChangePasswordRequest(
                testUser.plainPassword(),
                newPassword
            );

            mockMvc.perform(patch(CHANGE_PASSWORD_URL)
                    .header(AUTHORIZATION, BEARER + testUser.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            AccountEntity updatedAccount = accountRepository
                .findById(testUser.account().getAccountId()).orElseThrow();
            assertThat(passwordEncoder.matches(newPassword, updatedAccount.getPassword())).isTrue();
            String key = "jwt:revoke_all:" + testUser.account().getEmail().value();
            assertThat(redisTemplate.hasKey(key)).isTrue();

            waitUntilWireMockReceives(1);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when current password is wrong")
        void shouldReturnBadRequestWhenCurrentPasswordIsWrong() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest(
                "wrongPassword123",
                "NewPassword123!"
            );

            expectBadRequestFrom(
                mockMvc.perform(patch(CHANGE_PASSWORD_URL)
                    .header(AUTHORIZATION, BEARER + testUser.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("currentPassword", "Invalid current password");
        }

        @Test
        @DisplayName("Should return 400 Bad Request when new password is weak")
        void shouldReturnBadRequestWhenNewPasswordIsWeak() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest(
                testUser.plainPassword(),
                "weak"
            );

            expectBadRequestFrom(
                mockMvc.perform(patch(CHANGE_PASSWORD_URL)
                    .header(AUTHORIZATION, BEARER + testUser.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            ).forField("newPassword", "New password must have between 6 and 16 characters");
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when token is missing")
        void shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest(
                testUser.plainPassword(),
                "NewPassword123!"
            );

            mockMvc.perform(patch(CHANGE_PASSWORD_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }
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
