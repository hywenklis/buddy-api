package com.buddy.api.domains.terms.services;

import com.buddy.api.domains.terms.dtos.AcceptTermsDto;

public interface AcceptTerms {
    void accept(AcceptTermsDto acceptTermsDto);
}
