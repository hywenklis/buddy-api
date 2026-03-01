package com.buddy.api.domains.terms.repositories;

import com.buddy.api.domains.terms.entities.TermsVersionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TermsVersionRepository extends JpaRepository<TermsVersionEntity, UUID> {
    Optional<TermsVersionEntity> findFirstByIsActiveTrueOrderByPublicationDateDesc();

    Optional<TermsVersionEntity> findByVersionTag(String versionTag);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TermsVersionEntity t SET t.isActive = false WHERE t.isActive = true")
    int deactivateAllActive();
}
