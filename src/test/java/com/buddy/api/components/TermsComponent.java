package com.buddy.api.components;

import com.buddy.api.builders.terms.TermsBuilder;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.terms.entities.TermsVersionEntity;
import com.buddy.api.domains.terms.repositories.TermsVersionRepository;
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
                .versionTag("v1.0.0")
                .publishedBy(account)
                .build()
        );
    }

    public TermsVersionEntity createInactiveTerm() {
        return termsVersionRepository.save(
            TermsBuilder.validTermsVersionEntity()
                .isActive(false)
                .versionTag("v0.9.beta")
                .build()
        );
    }
}
