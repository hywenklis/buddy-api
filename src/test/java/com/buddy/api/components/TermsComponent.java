package com.buddy.api.components;

import com.buddy.api.builders.terms.TermsBuilder;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.terms.entities.TermsVersionEntity;
import com.buddy.api.domains.terms.repositories.TermsVersionRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class TermsComponent {

    private final TermsVersionRepository termsVersionRepository;

    public TermsComponent(final TermsVersionRepository termsVersionRepository) {
        this.termsVersionRepository = termsVersionRepository;
    }

    public TermsVersionEntity createActiveTerm(final AccountEntity account) {
        return termsVersionRepository.save(
            TermsBuilder.validTermsVersionEntity()
                .isActive(true)
                .versionTag(RandomStringUtils.secure().nextAlphanumeric(10))
                .publishedBy(account)
                .build()
        );
    }

    public TermsVersionEntity createInactiveTerm(final AccountEntity account) {
        return termsVersionRepository.save(
            TermsBuilder.validTermsVersionEntity()
                .isActive(false)
                .versionTag(RandomStringUtils.secure().nextAlphanumeric(10))
                .publishedBy(account)
                .build()
        );
    }
}
