package com.buddy.api.domains.profile.services;

import com.buddy.api.domains.profile.dtos.ProfileDto;
import java.util.List;
import java.util.UUID;

public interface FindProfile {
    List<ProfileDto> findByAccountId(UUID accountId);
}
