package com.buddy.api.integrations.web.account.controller;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static com.buddy.api.builders.account.AccountBuilder.validAccountRequest;
import static com.buddy.api.customverifications.CustomCreatedVerifications.expectCreatedFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static com.buddy.api.utils.RandomEmailUtils.generateValidEmail;
import static com.buddy.api.utils.RandomEmailUtils.generateValidEmailAddress;
import static com.buddy.api.utils.RandomStringUtils.ALPHABET;
import static com.buddy.api.utils.RandomStringUtils.generateRandomNumeric;
import static com.buddy.api.utils.RandomStringUtils.generateRandomString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.accounts.requests.AccountRequest;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("POST /v1/accounts/register")
public class CreateAccountControllerTest extends IntegrationTestAbstract {
    private static final String ACCOUNT_REGISTER_URL = "/v1/accounts/register";

    @Test
    @DisplayName("Should register a new account successfully")
    void register_new_account_success() throws Exception {
        var request = validAccountRequest().build();

        expectCreatedFrom(performCreateAccountRequest(request));
    }

    @Test
    @DisplayName("Should not register account if email is not filled in")
    void should_not_register_account_without_email() throws Exception {
        var request = validAccountRequest().email(null).build();

        expectEmailErrorReported(request, "Account email is mandatory");
    }

    @Test
    @DisplayName("Should not register account if email is invalid")
    void should_not_register_account_with_invalid_email() throws Exception {
        var request = validAccountRequest()
            .email(generateRandomString(10, ALPHABET))
            .build();

        expectEmailErrorReported(request, "Account email must be a valid email address");
    }

    @Test
    @DisplayName("Should not register account if email already belong to other account")
    void should_not_register_account_with_existing_account_email() throws Exception {
        var repeatedEmail = generateValidEmailAddress();

        accountRepository.save(validAccountEntity().email(repeatedEmail).build());

        var request = validAccountRequest()
            .email(repeatedEmail.value())
            .build();

        expectEmailErrorReported(request, "Account email already registered");
    }

    @Test
    @DisplayName("Should not register account if equivalent email is in the database")
    void should_not_register_account_with_equivalent_existing_account_email() throws Exception {
        var repeatedEmail = generateValidEmail().toLowerCase(Locale.ENGLISH);

        accountRepository.save(validAccountEntity().email(
            new EmailAddress(repeatedEmail)
        ).build());

        var request = validAccountRequest()
            .email(repeatedEmail.toUpperCase(Locale.ENGLISH))
            .build();

        expectEmailErrorReported(request, "Account email already registered");
    }

    @Test
    @DisplayName("Should not register account if password is not filled in")
    void should_not_register_account_without_password() throws Exception {
        var request = validAccountRequest().password(null).build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .forField("password", "Account password is mandatory");
    }

    @Test
    @DisplayName("Should not register account if password is too small")
    void should_not_register_account_with_small_password() throws Exception {
        var request = validAccountRequest().password(generateRandomString(5)).build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .forField(
                "password",
                "Account password must have between 6 and 16 characters"
            );
    }

    @Test
    @DisplayName("Should not register account if password is too big")
    void should_not_register_account_with_too_big_password() throws Exception {
        var request = validAccountRequest().password(generateRandomString(17)).build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .forField(
                "password",
                "Account password must have between 6 and 16 characters"
            );
    }

    @Test
    @DisplayName("Should not register account without terms of user consent information")
    void should_not_register_account_without_terms_of_user_consent_information() throws Exception {
        var request = validAccountRequest().termsOfUserConsent(null).build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .forField(
                "termsOfUserConsent",
                "Account terms of user consent information is mandatory"
            );
    }

    @Test
    @DisplayName("Should not register account if phone number is not a numeric string")
    void should_not_register_account_if_phone_number_is_not_numeric_string() throws Exception {
        var request = validAccountRequest().phoneNumber("99.123467").build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .forField(
                "phoneNumber",
                "Account phone number must contain only digits"
            );
    }

    @Test
    @DisplayName("Should not register account if phone number is too small")
    void should_not_register_account_if_phone_number_is_too_small() throws Exception {
        var request = validAccountRequest().phoneNumber(generateRandomNumeric(3)).build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .forField(
                "phoneNumber",
                "Account phone number must have between 4 and 20 digits"
            );
    }

    @Test
    @DisplayName("Should not register account if phone number is too big")
    void should_not_register_account_if_phone_number_is_too_big() throws Exception {
        var request = validAccountRequest().phoneNumber(generateRandomNumeric(21)).build();

        expectBadRequestFrom(performCreateAccountRequest(request))
            .forField(
                "phoneNumber",
                "Account phone number must have between 4 and 20 digits"
            );
    }

    private ResultActions performCreateAccountRequest(final AccountRequest request)
        throws Exception {
        return mockMvc
            .perform(post(ACCOUNT_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private void expectEmailErrorReported(final AccountRequest request, final String errorMessage)
        throws Exception {
        expectBadRequestFrom(performCreateAccountRequest(request))
            .forField("email", errorMessage);
    }
}
