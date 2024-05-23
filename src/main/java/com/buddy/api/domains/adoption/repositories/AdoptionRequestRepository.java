package com.buddy.api.domains.adoption.repositories;

import com.buddy.api.domains.adoption.entities.AdoptionRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequestEntity, Long> {
    List<AdoptionRequestEntity> findByPetId(Long petId);

    List<AdoptionRequestEntity> findByShelterId(Long shelterId);
}

