package com.buddy.api.domains.pet.repositories;

import com.buddy.api.domains.pet.entities.PetEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PetRepository
    extends JpaRepository<PetEntity, UUID>, JpaSpecificationExecutor<PetEntity> {
}