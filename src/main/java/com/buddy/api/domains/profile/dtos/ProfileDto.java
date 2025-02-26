package com.buddy.api.domains.profile.dtos;

import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ProfileDto(UUID accountId,
                         String name,
                         String description,
                         String bio,
                         Boolean isDeleted,
                         ProfileTypeEnum profileType) {
}
