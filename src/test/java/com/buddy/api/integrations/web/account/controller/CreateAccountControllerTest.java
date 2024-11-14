package com.buddy.api.integrations.web.account.controller;

import static com.buddy.api.utils.RandomEmailUtils.generateValidEmail;
import static com.buddy.api.utils.RandomStringUtils.generateRandomPassword;
import static com.buddy.api.utils.RandomStringUtils.generateRandomPhoneNumber;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.accounts.requests.AccountRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("POST /v1/accounts/register")
public class CreateAccountControllerTest extends IntegrationTestAbstract {
    private static final String ACCOUNT_REGISTER_URL = "/v1/accounts/register";

    @Test
    @DisplayName("Should register a new account successfully")
    void register_new_account_success() throws Exception {
        var termsOfUserConsent = true;

        var request = new AccountRequest(
            generateValidEmail(),
            generateRandomPhoneNumber(),
            generateRandomPassword(),
            termsOfUserConsent
        );

        mockMvc
            .perform(post(ACCOUNT_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("successfully created"));
    }
}
