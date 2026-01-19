package com.buddy.api.domains.terms.services.impls;

import com.buddy.api.domains.terms.dtos.TermsAcceptanceDto;
import com.buddy.api.domains.terms.mappers.TermsMapper;
import com.buddy.api.domains.terms.repositories.TermsAcceptanceRepository;
import com.buddy.api.domains.terms.services.CreateTermsAcceptance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateTermsAcceptanceImpl implements CreateTermsAcceptance {

    private final TermsAcceptanceRepository termsAcceptanceRepository;
    private final TermsMapper termsMapper;

    @Override
    @Transactional
    public void create(final TermsAcceptanceDto termsAcceptanceDto) {
        final var termsAcceptanceEntity = termsMapper.toTermsAcceptanceEntity(termsAcceptanceDto);
        termsAcceptanceRepository.save(termsAcceptanceEntity);
    }
}
