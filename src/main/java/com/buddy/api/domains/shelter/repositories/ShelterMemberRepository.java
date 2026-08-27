package com.buddy.api.domains.shelter.repositories;

import com.buddy.api.domains.shelter.entities.ShelterMemberEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShelterMemberRepository extends JpaRepository<ShelterMemberEntity, UUID> {
}
