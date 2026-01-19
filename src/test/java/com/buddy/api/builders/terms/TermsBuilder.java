package com.buddy.api.builders.terms;

import static com.buddy.api.builders.account.AccountBuilder.validAccountDto;

import com.buddy.api.domains.terms.dtos.AcceptTermsDto;
import com.buddy.api.domains.terms.dtos.TermsAcceptanceDto;
import com.buddy.api.domains.terms.dtos.TermsVersionDto;
import com.buddy.api.domains.terms.entities.TermsVersionEntity;
import com.buddy.api.utils.RandomEmailUtils;
import java.time.LocalDate;
import org.apache.commons.lang3.RandomStringUtils;

public class TermsBuilder {
    public static AcceptTermsDto.AcceptTermsDtoBuilder validAcceptTermsDto() {
        return AcceptTermsDto.builder()
            .email(RandomEmailUtils.generateValidEmail())
            .ipAddress(RandomStringUtils.secure().nextAlphanumeric(10))
            .userAgent(RandomStringUtils.secure().nextAlphabetic(10));
    }

    public static TermsVersionDto.TermsVersionDtoBuilder validTermsVersionDto() {
        return TermsVersionDto.builder()
            .versionTag(RandomStringUtils.secure().nextAlphanumeric(10))
            .content(RandomStringUtils.secure().nextAlphanumeric(10))
            .isActive(true)
            .publicationDate(LocalDate.now());
    }

    public static TermsVersionEntity.TermsVersionEntityBuilder validTermsVersionEntity() {
        return TermsVersionEntity.builder()
            .versionTag(RandomStringUtils.secure().nextAlphanumeric(10))
            .content(RandomStringUtils.secure().nextAlphanumeric(10))
            .isActive(true)
            .publicationDate(LocalDate.now());
    }

    public static TermsAcceptanceDto.TermsAcceptanceDtoBuilder validTermsAcceptanceDto() {
        return TermsAcceptanceDto.builder()
            .account(validAccountDto().build())
            .termsVersion(validTermsVersionDto().build())
            .ipAddress("0.0.0.1")
            .userAgent("Mozilla/5.0");
    }
}
