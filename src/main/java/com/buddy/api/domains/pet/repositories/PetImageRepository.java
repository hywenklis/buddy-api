package com.buddy.api.domains.pet.repositories;

import com.buddy.api.domains.pet.entities.PetImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PetImageRepository extends JpaRepository<PetImageEntity, UUID> {
}

