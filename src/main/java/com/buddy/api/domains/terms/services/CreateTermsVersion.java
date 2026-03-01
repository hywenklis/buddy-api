package com.buddy.api.domains.terms.services;

import com.buddy.api.domains.terms.dtos.CreateTermsVersionDto;
import com.buddy.api.domains.terms.dtos.TermsVersionDto;

public interface CreateTermsVersion {
    TermsVersionDto create(CreateTermsVersionDto dto);
}
