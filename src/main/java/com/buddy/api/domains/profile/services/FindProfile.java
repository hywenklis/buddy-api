package com.buddy.api.domains.profile.services;

import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FindProfile {

    List<ProfileDto> findByAccountEmail(String email);

    List<ProfileEntity> findActiveProfilesByAccountId(UUID accountId);

    Optional<ProfileEntity> findActiveProfileByAccountIdAndType(
        UUID accountId,
        ProfileTypeEnum profileType
    );

    Optional<ProfileEntity> findActiveShelterProfileByAccountId(UUID accountId);

    Optional<ProfileEntity> findActiveByIdAndAccountId(UUID profileId, UUID accountId);

    Optional<ProfileEntity> findById(UUID profileId);
}
