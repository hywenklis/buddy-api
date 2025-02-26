package com.buddy.api.domains.profile.repositories;

import com.buddy.api.domains.profile.entities.ProfileEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID> {

    Optional<List<ProfileEntity>> findByAccountAccountId(UUID accountId);

    Boolean existsByName(String name);
}
