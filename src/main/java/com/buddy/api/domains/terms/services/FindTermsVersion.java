package com.buddy.api.domains.terms.services;

import com.buddy.api.domains.terms.dtos.TermsVersionDto;
import java.util.Optional;

public interface FindTermsVersion {
    TermsVersionDto findActive();

    Optional<TermsVersionDto> findByTag(String versionTag);
}
