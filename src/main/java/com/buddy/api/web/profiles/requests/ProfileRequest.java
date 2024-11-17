package com.buddy.api.web.profiles.requests;

import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import java.util.UUID;

public record ProfileRequest(UUID accountId,
                             String name,
                             String description,
                             String bio,
                             ProfileTypeEnum profileType) {
}
