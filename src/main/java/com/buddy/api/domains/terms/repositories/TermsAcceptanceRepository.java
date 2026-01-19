package com.buddy.api.domains.terms.repositories;

import com.buddy.api.domains.terms.entities.TermsAcceptanceEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsAcceptanceRepository extends JpaRepository<TermsAcceptanceEntity, UUID> {
    boolean existsByAccountAccountIdAndTermsVersionTermsVersionId(final UUID accountId,
                                                                  final UUID termsVersionId
    );
}
