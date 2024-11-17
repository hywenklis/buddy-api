package com.buddy.api.web.profiles.requests;

import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ProfileRequest(
    @NotNull(message = "Profile account ID is mandatory")
    UUID accountId,
    String name,
    String description,
    String bio,
    ProfileTypeEnum profileType
) {
}
