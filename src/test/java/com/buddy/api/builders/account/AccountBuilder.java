package com.buddy.api.builders.account;

import static com.buddy.api.utils.RandomEmailUtils.generateValidEmail;
import static com.buddy.api.utils.RandomStringUtils.generateRandomPassword;
import static com.buddy.api.utils.RandomStringUtils.generateRandomPhoneNumber;

import com.buddy.api.web.accounts.requests.AccountRequest;

public class AccountBuilder {
    public static AccountRequest.AccountRequestBuilder validAccountRequest() {
        return AccountRequest
            .builder()
            .email(generateValidEmail())
            .password(generateRandomPassword())
            .phoneNumber(generateRandomPhoneNumber())
            .termsOfUserConsent(true);
    }
}
