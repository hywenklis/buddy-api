package com.buddy.api.domains.pet.repositories;

import com.buddy.api.domains.pet.entities.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PetRepository extends JpaRepository<PetEntity, UUID> {
}

