package com.buddy.api.domains.adoption.repositories;

import com.buddy.api.domains.adoption.entities.AdoptionRequestEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequestEntity, UUID> {
}
