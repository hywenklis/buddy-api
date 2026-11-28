package com.buddy.api.domains.pet.repositories;

import com.buddy.api.domains.pet.entities.PetV2Entity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PetV2Repository extends JpaRepository<PetV2Entity, UUID>,
    JpaSpecificationExecutor<PetV2Entity> {
}
