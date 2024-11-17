package com.buddy.api.integrations.web.profile.controller;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static com.buddy.api.builders.profile.ProfileBuilder.profileRequest;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.profiles.requests.ProfileRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("POST /v1/profiles/register")
public class CreateProfileControllerTest extends IntegrationTestAbstract {

    private static final String PROFILE_REGISTER_URL = "/v1/profiles/register";
    private static final String ERROR_FIELD_PATH = "$.errors[0].field";
    private static final String ERROR_MESSAGE_PATH = "$.errors[0].message";
    private static final String ERROR_HTTP_STATUS_PATH = "$.errors[0].httpStatus";
    private static final String ERROR_CODE_PATH = "$.errors[0].errorCode";
    private static final String ERROR_TIMESTAMP_PATH = "$.errors[0].timestamp";

    @Test
    @DisplayName("Should register a new profile successfuly")
    void should_register_new_profile() throws Exception {
        final var account = accountRepository.save(validAccountEntity().build());
        final var request = profileRequest()
            .accountId(account.getAccountId())
            .build();

        perfomCreateProfileRequest(request)
            .andExpectAll(
                status().isCreated(),
                jsonPath("$.message").value("Successfully created")
            );
    }

    @Test
    @DisplayName("Should not create profile without account id")
    void should_not_create_profile_without_account_id() throws Exception {
        final var request = profileRequest().accountId(null).build();

        expectBadRequestFrom(perfomCreateProfileRequest(request))
            .andExpectAll(
                jsonPath(ERROR_FIELD_PATH).value("accountId"),
                jsonPath(ERROR_MESSAGE_PATH).value(
                    "Profile account ID is mandatory"
                )
            );
    }

    private ResultActions expectBadRequestFrom(final ResultActions result) throws Exception {
        return result.andExpectAll(
            status().isBadRequest(),
            jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST.name()),
            jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()),
            jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty()
        );
    }

    private ResultActions perfomCreateProfileRequest(final ProfileRequest request)
        throws Exception {
        return mockMvc
            .perform(post(PROFILE_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
