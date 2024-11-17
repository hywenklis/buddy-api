package com.buddy.api.integrations.web.profile.controller;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static com.buddy.api.builders.profile.ProfileBuilder.profileRequest;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.profiles.requests.ProfileRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("POST /v1/profiles/register")
public class CreateProfileControllerTest extends IntegrationTestAbstract {

    private static final String PROFILE_REGISTER_URL = "/v1/profiles/register";

    @Test
    @DisplayName("Should register a new profile successfuly")
    void should_register_new_profile() throws Exception {
        final var account = accountRepository.save(validAccountEntity().build());
        final var request = profileRequest()
            .accountId(account.getAccountId())
            .build();

        performCreateProfileRequest(request)
            .andExpectAll(
                status().isCreated(),
                jsonPath("$.message").value("Successfully created")
            );
    }

    @Test
    @DisplayName("Should not create profile without account id")
    void should_not_create_profile_without_account_id() throws Exception {
        final var request = profileRequest().accountId(null).build();

        expectBadRequestFrom(performCreateProfileRequest(request))
            .forField("accountId", "Profile account ID is mandatory");
    }

    private ResultActions performCreateProfileRequest(final ProfileRequest request)
        throws Exception {
        return mockMvc
            .perform(post(PROFILE_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
