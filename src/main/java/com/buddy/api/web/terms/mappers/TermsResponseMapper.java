package com.buddy.api.web.terms.mappers;

import com.buddy.api.domains.terms.dtos.TermsVersionDto;
import com.buddy.api.web.terms.responses.TermsVersionResponse;
import org.mapstruct.Mapper;

@Mapper
public interface TermsResponseMapper {
    TermsVersionResponse toTermsVersionResponse(final TermsVersionDto termsVersionDto);
}
