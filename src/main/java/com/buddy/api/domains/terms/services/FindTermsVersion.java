package com.buddy.api.domains.terms.services;

import com.buddy.api.domains.terms.dtos.TermsVersionDto;

public interface FindTermsVersion {
    TermsVersionDto findActive();
}
