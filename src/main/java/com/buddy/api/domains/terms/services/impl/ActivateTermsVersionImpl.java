package com.buddy.api.domains.terms.services.impl;

import com.buddy.api.domains.terms.repositories.TermsVersionRepository;
import com.buddy.api.domains.terms.services.ActivateTermsVersion;
import com.buddy.api.domains.terms.services.FindTermsVersion;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivateTermsVersionImpl implements ActivateTermsVersion {

    private final TermsVersionRepository termsVersionRepository;
    private final FindTermsVersion findTermsVersion;

    @Override
    @Transactional
    @CacheEvict(value = "terms", key = "'active'")
    public void activate(final UUID termsVersionId) {
        log.info("Activating terms version with id: {}", termsVersionId);

        final var termsVersion = findTermsVersion.findById(termsVersionId);

        if (termsVersion.isActive()) {
            log.info("Terms version is already active, returning current state");
            return;
        }

        termsVersionRepository.deactivateAllActive();
        termsVersionRepository.activateById(termsVersionId);

        log.info("Terms version activated successfully with id: {}", termsVersionId);
    }
}
