package com.buddy.api.domains.profile.services.impl;

import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.mappers.ProfileMapper;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import com.buddy.api.domains.profile.services.FindProfile;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindProfileImpl implements FindProfile {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProfileDto> findByAccountId(final UUID accountId) {
        return profileRepository.findByAccountAccountId(accountId)
            .map(profileMapper::toProfilesDto)
            .orElse(Collections.emptyList());
    }
}
