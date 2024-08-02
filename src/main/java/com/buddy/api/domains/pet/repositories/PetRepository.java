package com.buddy.api.domains.pet.repositories;

import com.buddy.api.domains.pet.entities.PetEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PetRepository extends JpaRepository<PetEntity, UUID>,
    JpaSpecificationExecutor<PetEntity> {

    @EntityGraph(attributePaths = {"images", "shelter"})
    List<PetEntity> findAll(Specification<PetEntity> spec);

    @EntityGraph(attributePaths = {"images", "shelter"})
    Page<PetEntity> findAll(Specification<PetEntity> spec, Pageable pageable);
}
