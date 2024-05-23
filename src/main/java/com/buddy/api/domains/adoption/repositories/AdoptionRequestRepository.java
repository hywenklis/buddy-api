package com.buddy.api.domains.adoption.repositories;

import com.buddy.api.domains.adoption.entities.AdoptionRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequestEntity, UUID> {
}

