package com.buddy.api.domains.profile.repositories;

import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.domains.valueobjects.EmailAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID> {

    Optional<List<ProfileEntity>> findByAccountEmail(EmailAddress emailAddress);

    List<ProfileEntity> findByAccount_AccountIdAndIsDeletedFalse(UUID accountId);

    Optional<ProfileEntity> findByAccount_AccountIdAndProfileTypeAndIsDeletedFalse(
        UUID accountId,
        ProfileTypeEnum profileType
    );

    Optional<ProfileEntity> findByProfileIdAndAccount_AccountIdAndIsDeletedFalse(
        UUID profileId,
        UUID accountId
    );

    Boolean existsByName(String name);
}
