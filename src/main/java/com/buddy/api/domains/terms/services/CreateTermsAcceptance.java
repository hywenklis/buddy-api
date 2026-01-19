package com.buddy.api.domains.terms.services;

import com.buddy.api.domains.terms.dtos.TermsAcceptanceDto;

public interface CreateTermsAcceptance {
    void create(TermsAcceptanceDto termsAcceptanceDto);
}
