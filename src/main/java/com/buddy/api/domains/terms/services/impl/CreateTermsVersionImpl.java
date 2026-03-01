package com.buddy.api.domains.terms.services.impl;

import com.buddy.api.commons.exceptions.VersionTagAlreadyExistsException;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.terms.dtos.CreateTermsVersionDto;
import com.buddy.api.domains.terms.dtos.TermsVersionDto;
import com.buddy.api.domains.terms.mappers.TermsMapper;
import com.buddy.api.domains.terms.repositories.TermsVersionRepository;
import com.buddy.api.domains.terms.services.CreateTermsVersion;
import com.buddy.api.domains.terms.services.FindTermsVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateTermsVersionImpl implements CreateTermsVersion {

    private final TermsVersionRepository termsVersionRepository;
    private final FindAccount findAccount;
    private final FindTermsVersion findTermsVersion;
    private final TermsMapper termsMapper;

    @Override
    @Transactional
    @CacheEvict(value = "terms", key = "'active'", condition = "#dto.isActive() == true")
    public TermsVersionDto create(final CreateTermsVersionDto dto) {
        log.info("Creating new terms version with tag: {}", dto.versionTag());

        validateVersionTagNotExists(dto.versionTag());

        final var publishedByDto = findAccount.findByEmail(dto.publishedByAccountEmail());
        final var isActive = Boolean.TRUE.equals(dto.isActive());

        if (isActive) {
            log.info("Deactivating all currently active terms versions before DB change");
            termsVersionRepository.deactivateAllActive();
            log.info("Deactivated all currently active terms versions after DB change");
        }

        final var entityToSave = termsMapper.toTermsVersionEntity(dto, publishedByDto, isActive);

        log.info("Saving new terms version to database");
        final var savedEntity = termsVersionRepository.save(entityToSave);
        log.info("Terms version created successfully with id: {}", savedEntity.getTermsVersionId());

        return termsMapper.toTermsVersionDto(savedEntity);
    }

    private void validateVersionTagNotExists(final String versionTag) {
        findTermsVersion.findByTag(versionTag)
            .ifPresent(existing -> {
                log.debug("Version tag already exists: {}", versionTag);
                throw new VersionTagAlreadyExistsException(versionTag);
            });
    }
}
