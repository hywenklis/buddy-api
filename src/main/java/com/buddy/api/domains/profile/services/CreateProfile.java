package com.buddy.api.domains.profile.services;

import com.buddy.api.domains.profile.dtos.ProfileDto;
import java.util.UUID;

public interface CreateProfile {
    void create(ProfileDto profileDto, UUID authenticatedUserId);
}
