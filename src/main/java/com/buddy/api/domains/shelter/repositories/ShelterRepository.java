package com.buddy.api.domains.shelter.repositories;

import com.buddy.api.domains.shelter.entities.ShelterEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShelterRepository extends JpaRepository<ShelterEntity, UUID> {

    Optional<ShelterEntity> findShelterByCpfResponsible(final String cpfResponsible);

    Optional<ShelterEntity> findByEmail(final String email);
}
