package com.buddy.api.integrations.web.profile.controller;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static com.buddy.api.utils.RandomStringUtils.generateRandomString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.profiles.requests.ProfileRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("POST /v1/profiles/register")
public class CreateProfileControllerTest extends IntegrationTestAbstract {

    @Test
    @DisplayName("Should register a new profile successfuly")
    void should_register_new_profile() throws Exception {
        var account = accountRepository.save(validAccountEntity().build());
        String name = generateRandomString(6);
        String description = generateRandomString(10);
        String bio = generateRandomString(10);
        ProfileTypeEnum profileType = ProfileTypeEnum.ADOPTER;

        var request = new ProfileRequest(
            account.getAccountId(),
            name,
            description,
            bio,
            profileType
        );

        mockMvc
            .perform(post("/v1/profiles/register")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpectAll(
                status().isCreated(),
                jsonPath("$.message").value("Successfully created")
            );
    }
}
