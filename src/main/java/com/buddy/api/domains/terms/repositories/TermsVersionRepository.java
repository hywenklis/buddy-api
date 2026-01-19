package com.buddy.api.domains.terms.repositories;

import com.buddy.api.domains.terms.entities.TermsVersionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsVersionRepository extends JpaRepository<TermsVersionEntity, UUID> {
    Optional<TermsVersionEntity> findFirstByIsActiveTrueOrderByPublicationDateDesc();
}
