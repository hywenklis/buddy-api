package com.buddy.api.domains.terms.services.impl;

import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.terms.dtos.TermsVersionDto;
import com.buddy.api.domains.terms.mappers.TermsMapper;
import com.buddy.api.domains.terms.repositories.TermsVersionRepository;
import com.buddy.api.domains.terms.services.FindTermsVersion;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

    @Override
    @Transactional(readOnly = true)
    public TermsVersionDto findById(final UUID termsVersionId) {
        final var entity = termsVersionRepository.findById(termsVersionId)
            .orElseThrow(() -> new NotFoundException(
                "termsVersionId",
                "Terms version not found with id: " + termsVersionId));
        return termsMapper.toTermsVersionDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TermsVersionDto> findByTag(final String versionTag) {
        log.info("Searching for terms version with tag: {}", versionTag);
        final var entityOptional = termsVersionRepository.findByVersionTag(versionTag);
        log.info("Finished database search for terms version tag: {}", versionTag);

        return entityOptional.map(termsMapper::toTermsVersionDto);
    }
}
