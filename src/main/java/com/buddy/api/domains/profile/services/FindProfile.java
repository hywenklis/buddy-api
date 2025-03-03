package com.buddy.api.domains.profile.services;

import com.buddy.api.domains.profile.dtos.ProfileDto;
import java.util.List;

public interface FindProfile {
    List<ProfileDto> findByAccountEmail(String email);
}
