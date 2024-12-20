package com.buddy.api.builders.account;

import static com.buddy.api.utils.RandomEmailUtils.generateValidEmail;
import static com.buddy.api.utils.RandomEmailUtils.generateValidEmailAddress;
import static com.buddy.api.utils.RandomStringUtils.generateRandomPassword;
import static com.buddy.api.utils.RandomStringUtils.generateRandomPhoneNumber;

import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.entities.AccountEntity;
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

    public static AccountEntity.AccountEntityBuilder validAccountEntity() {
        return AccountEntity
            .builder()
            .email(generateValidEmailAddress())
            .phoneNumber(generateRandomPhoneNumber())
            .password(generateRandomPassword())
            .termsOfUserConsent(true)
            .isBlocked(false)
            .isVerified(false)
            .isDeleted(false);
    }

    public static AccountDto.AccountDtoBuilder validAccountDto() {
        return AccountDto
            .builder()
            .email(generateValidEmailAddress())
            .phoneNumber(generateRandomPhoneNumber())
            .password(generateRandomPassword())
            .termsOfUserConsent(true);
    }
}
