package com.buddy.api.domains.terms.services;

import com.buddy.api.domains.terms.dtos.TermsVersionDto;
import java.util.Optional;
import java.util.UUID;

public interface FindTermsVersion {
    TermsVersionDto findActive();

    TermsVersionDto findById(UUID termsVersionId);

    Optional<TermsVersionDto> findByTag(String versionTag);
}
