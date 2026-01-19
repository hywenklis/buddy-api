package com.buddy.api.domains.terms.services.impls;

import com.buddy.api.domains.terms.repositories.TermsAcceptanceRepository;
import com.buddy.api.domains.terms.services.FindTermsAcceptance;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindTermsAcceptanceImpl implements FindTermsAcceptance {

    private final TermsAcceptanceRepository termsAcceptanceRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean exists(final UUID accountId, final UUID termsVersionId) {
        return termsAcceptanceRepository.existsByAccountAccountIdAndTermsVersionTermsVersionId(
            accountId,
            termsVersionId
        );
    }
}
