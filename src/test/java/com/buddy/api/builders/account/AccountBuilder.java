package com.buddy.api.builders.account;

import static com.buddy.api.utils.RandomEmailUtils.generateValidEmail;
import static com.buddy.api.utils.RandomEmailUtils.generateValidEmailAddress;

import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.web.accounts.requests.AccountRequest;
import org.apache.commons.lang3.RandomStringUtils;

public class AccountBuilder {
    public static AccountRequest.AccountRequestBuilder validAccountRequest() {
        return AccountRequest
            .builder()
            .email(generateValidEmail())
            .password(RandomStringUtils.secure().nextAlphanumeric(10))
            .phoneNumber(RandomStringUtils.secure().nextNumeric(9))
            .termsOfUserConsent(true);
    }

    public static AccountEntity.AccountEntityBuilder validAccountEntity() {
        return AccountEntity
            .builder()
            .email(generateValidEmailAddress())
            .phoneNumber(RandomStringUtils.secure().nextNumeric(9))
            .password(RandomStringUtils.secure().nextAlphanumeric(10))
            .termsOfUserConsent(true)
            .isBlocked(false)
            .isVerified(false)
            .isDeleted(false);
    }

    public static AccountDto.AccountDtoBuilder validAccountDto() {
        return AccountDto
            .builder()
            .email(generateValidEmailAddress())
            .phoneNumber(RandomStringUtils.secure().nextNumeric(9))
            .password(RandomStringUtils.secure().nextAlphanumeric(10))
            .termsOfUserConsent(true);
    }
}
