package com.buddy.api.integrations.web.profile.controller;

import static com.buddy.api.builders.profile.ProfileBuilder.profileRequest;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static com.buddy.api.utils.RandomStringUtils.generateRandomString;
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
        final var request = profileComponent.validProfileRequest().build();

        performCreateProfileRequest(request)
            .andExpectAll(
                status().isCreated(),
                jsonPath("$.message").value("successfully created")
            );
    }

    @Test
    @DisplayName("Should not create profile without account id")
    void should_not_create_profile_without_account_id() throws Exception {
        final var request = profileRequest().accountId(null).build();

        expectBadRequestFrom(performCreateProfileRequest(request))
            .forField("accountId", "Profile account ID is mandatory");
    }

    @Test
    @DisplayName("Should not create profile without name")
    void should_not_create_profile_without_name() throws Exception {
        final var request = profileComponent
            .validProfileRequest()
            .name(null)
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request))
            .forField("name", "Profile name is mandatory");
    }

    @Test
    @DisplayName("Should not create profile when name is too small")
    void should_not_create_profile_when_name_is_too_small() throws Exception {
        final var request = profileComponent
            .validProfileRequest()
            .name(generateRandomString(2))
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request))
            .forField("name", "Profile name must have between 3 and 100 characters");
    }

    @Test
    @DisplayName("Should not create profile when name is too big")
    void should_not_create_profile_when_name_is_too_big() throws Exception {
        final var request = profileComponent
            .validProfileRequest()
            .name(generateRandomString(101))
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request))
            .forField("name", "Profile name must have between 3 and 100 characters");
    }

    @Test
    @DisplayName("Should not create profile when description is too big")
    void should_not_create_profile_when_description_too_big() throws Exception {
        final var request = profileComponent
            .validProfileRequest()
            .description(generateRandomString(256))
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request))
            .forField(
                "description",
                "Profile description must have at most 255 characters"
            );
    }

    @Test
    @DisplayName("Should not create profile without profile type")
    void should_not_create_profile_without_profile_type() throws Exception {
        final var request = profileComponent
            .validProfileRequest()
            .profileType(null)
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request))
            .forField("profileType", "Profile type is mandatory");
    }

    private ResultActions performCreateProfileRequest(final ProfileRequest request)
        throws Exception {
        return mockMvc
            .perform(post(PROFILE_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
