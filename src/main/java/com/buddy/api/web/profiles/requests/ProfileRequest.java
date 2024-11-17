package com.buddy.api.web.profiles.requests;

import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ProfileRequest(
    @NotNull(message = "Profile account ID is mandatory")
    UUID accountId,
    @NotBlank(message = "Profile name is mandatory")
    @Size(message = "Profile name must have between 3 and 100 characters", min = 3, max = 100)
    String name,
    @Size(message = "Profile description must have at most 255 characters", max = 255)
    String description,
    String bio,
    @NotNull(message = "Profile type is mandatory")
    @Enumerated(EnumType.STRING)
    ProfileTypeEnum profileType
) {
}
