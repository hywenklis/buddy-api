package com.buddy.api.domains.terms.services.impl;

import com.buddy.api.commons.exceptions.VersionTagAlreadyExistsException;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.terms.dtos.CreateTermsVersionDto;
import com.buddy.api.domains.terms.dtos.TermsVersionDto;
import com.buddy.api.domains.terms.mappers.TermsMapper;
import com.buddy.api.domains.terms.repositories.TermsVersionRepository;
import com.buddy.api.domains.terms.services.ActivateTermsVersion;
import com.buddy.api.domains.terms.services.CreateTermsVersion;
import com.buddy.api.domains.terms.services.FindTermsVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateTermsVersionImpl implements CreateTermsVersion {

    private final TermsVersionRepository termsVersionRepository;
    private final FindAccount findAccount;
    private final FindTermsVersion findTermsVersion;
    private final ActivateTermsVersion activateTermsVersion;
    private final TermsMapper termsMapper;

    @Override
    @Transactional
    public TermsVersionDto create(final CreateTermsVersionDto dto) {
        log.info("Creating new terms version with tag: {}", dto.versionTag());

        validateVersionTagNotExists(dto.versionTag());

        final var publishedByDto = findAccount.findByEmail(dto.publishedByAccountEmail());
        final var entityToSave = termsMapper.toTermsVersionEntity(dto, publishedByDto, false);

        log.info("Saving new terms version to database as inactive draft");
        final var savedEntity = termsVersionRepository.save(entityToSave);

        if (Boolean.TRUE.equals(dto.isActive())) {
            log.info("Delegating to ActivateTermsVersion to set as active atomically");
            activateTermsVersion.activate(savedEntity.getTermsVersionId());
            savedEntity.setIsActive(true);
        }

        log.info("Terms version created successfully with id: {}", savedEntity.getTermsVersionId());
        return termsMapper.toTermsVersionDto(savedEntity);
    }

    private void validateVersionTagNotExists(final String versionTag) {
        if (findTermsVersion.findByTag(versionTag).isPresent()) {
            log.warn("Version tag already exists: {}", versionTag);
            throw new VersionTagAlreadyExistsException(versionTag);
        }
    }
}
