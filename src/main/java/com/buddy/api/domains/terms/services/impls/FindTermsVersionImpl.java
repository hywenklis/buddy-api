package com.buddy.api.domains.terms.services.impls;

import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.terms.dtos.TermsVersionDto;
import com.buddy.api.domains.terms.mappers.TermsMapper;
import com.buddy.api.domains.terms.repositories.TermsVersionRepository;
import com.buddy.api.domains.terms.services.FindTermsVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindTermsVersionImpl implements FindTermsVersion {

    private final TermsVersionRepository termsVersionRepository;
    private final TermsMapper termsMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "terms", key = "'active'", unless = "#result == null")
    public TermsVersionDto findActive() {
        return termsMapper.toTermsVersionDto(
            termsVersionRepository.findFirstByIsActiveTrueOrderByPublicationDateDesc()
                .orElseThrow(() -> new NotFoundException("terms", "No active terms found")));
    }
}
