package com.buddy.api.integrations.web.account.controller;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static com.buddy.api.builders.account.AccountBuilder.validAccountRequest;
import static com.buddy.api.utils.RandomEmailUtils.generateValidEmail;
import static com.buddy.api.utils.RandomStringUtils.ALPHABET;
import static com.buddy.api.utils.RandomStringUtils.generateRandomNumeric;
import static com.buddy.api.utils.RandomStringUtils.generateRandomString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.accounts.requests.AccountRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("POST /v1/accounts/register")
public class CreateAccountControllerTest extends IntegrationTestAbstract {
    private static final String ACCOUNT_REGISTER_URL = "/v1/accounts/register";
    private static final String ERROR_FIELD_PATH = "$.errors[0].field";
    private static final String ERROR_MESSAGE_PATH = "$.errors[0].message";
    private static final String ERROR_HTTP_STATUS_PATH = "$.errors[0].httpStatus";
    private static final String ERROR_CODE_PATH = "$.errors[0].errorCode";
    private static final String ERROR_TIMESTAMP_PATH = "$.errors[0].timestamp";

    @Test
    @DisplayName("Should register a new account successfully")
    void register_new_account_success() throws Exception {
        var request = validAccountRequest().build();

        performCreateAccountRequest(request)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("successfully created"));
    }

    @Test
    @DisplayName("Should not register account if email is not filled in")
    void should_not_register_account_without_email() throws Exception {
        var request = validAccountRequest().email(null).build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .andExpectAll(
                jsonPath(ERROR_FIELD_PATH).value("email"),
                jsonPath(ERROR_MESSAGE_PATH).value("Account email is mandatory")
            );
    }

    @Test
    @DisplayName("Should not register account if email is invalid")
    void should_not_register_account_with_invalid_email() throws Exception {
        var request = validAccountRequest()
            .email(generateRandomString(10, ALPHABET))
            .build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .andExpectAll(
                jsonPath(ERROR_FIELD_PATH).value("email"),
                jsonPath(ERROR_MESSAGE_PATH).value(
                    "Account email must be a valid email address"
                )
            );
    }

    @Test
    @DisplayName("Should not register account if email already belong to other account")
    void should_not_register_account_with_existing_account_email() throws Exception {
        var repeatedEmail = generateValidEmail();

        accountRepository.save(validAccountEntity().email(repeatedEmail).build());

        var request = validAccountRequest()
            .email(repeatedEmail)
            .build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .andExpectAll(
                jsonPath(ERROR_FIELD_PATH).value("email"),
                jsonPath(ERROR_MESSAGE_PATH).value(
                    "Account email already registered"
                )
            );
    }

    @Test
    @DisplayName("Should not register account if password is not filled in")
    void should_not_register_account_without_password() throws Exception {
        var request = validAccountRequest().password(null).build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .andExpectAll(
                jsonPath(ERROR_FIELD_PATH).value("password"),
                jsonPath(ERROR_MESSAGE_PATH).value(
                    "Account password is mandatory"
                )
            );
    }

    @Test
    @DisplayName("Should not register account if password is too small")
    void should_not_register_account_with_small_password() throws Exception {
        var request = validAccountRequest().password(generateRandomString(5)).build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .andExpectAll(
                jsonPath(ERROR_FIELD_PATH).value("password"),
                jsonPath(ERROR_MESSAGE_PATH).value(
                    "Account password must have between 6 and 16 characters"
                )
            );
    }

    @Test
    @DisplayName("Should not register account if password is too big")
    void should_not_register_account_with_too_big_password() throws Exception {
        var request = validAccountRequest().password(generateRandomString(17)).build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .andExpectAll(
                jsonPath(ERROR_FIELD_PATH).value("password"),
                jsonPath(ERROR_MESSAGE_PATH).value(
                    "Account password must have between 6 and 16 characters"
                )
            );
    }

    @Test
    @DisplayName("Should not register account without terms of user consent information")
    void should_not_register_account_without_terms_of_user_consent_information() throws Exception {
        var request = validAccountRequest().termsOfUserConsent(null).build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .andExpectAll(
                jsonPath(ERROR_FIELD_PATH).value("termsOfUserConsent"),
                jsonPath(ERROR_MESSAGE_PATH).value(
                    "Account terms of user consent information is mandatory"
                )
            );
    }

    @Test
    @DisplayName("Should not register account if phone number is not a numeric string")
    void should_not_register_account_if_phone_number_is_not_numeric_string() throws Exception {
        var request = validAccountRequest().phoneNumber("99.123467").build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .andExpectAll(
                jsonPath(ERROR_FIELD_PATH).value("phoneNumber"),
                jsonPath(ERROR_MESSAGE_PATH).value(
                    "Account phone number must contain only digits"
                )
            );
    }

    @Test
    @DisplayName("Should not register account if phone number is too small")
    void should_not_register_account_if_phone_number_is_too_small() throws Exception {
        var request = validAccountRequest().phoneNumber(generateRandomNumeric(3)).build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .andExpectAll(
                jsonPath(ERROR_FIELD_PATH).value("phoneNumber"),
                jsonPath(ERROR_MESSAGE_PATH).value(
                    "Account phone number must have between 4 and 20 digits"
                )
            );
    }

    @Test
    @DisplayName("Should not register account if phone number is too big")
    void should_not_register_account_if_phone_number_is_too_big() throws Exception {
        var request = validAccountRequest().phoneNumber(generateRandomNumeric(21)).build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .andExpectAll(
                jsonPath(ERROR_FIELD_PATH).value("phoneNumber"),
                jsonPath(ERROR_MESSAGE_PATH).value(
                    "Account phone number must have between 4 and 20 digits"
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

    private ResultActions performCreateAccountRequest(final AccountRequest request)
        throws Exception {
        return mockMvc
            .perform(post(ACCOUNT_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
