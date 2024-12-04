package com.buddy.api.domains.profile.services;

import com.buddy.api.domains.profile.dtos.ProfileDto;

public interface CreateProfile {
    void create(ProfileDto profileDto);
}
