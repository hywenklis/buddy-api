package com.buddy.api.domains.profile.services.impl;

import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.mappers.ProfileMapper;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import com.buddy.api.domains.profile.services.FindProfile;
import com.buddy.api.domains.valueobjects.EmailAddress;
import java.util.Collections;
import java.util.List;
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
    public List<ProfileDto> findByAccountEmail(final String email) {
        return profileRepository.findByAccountEmail(new EmailAddress(email))
            .map(profileMapper::toProfilesDto)
            .orElse(Collections.emptyList());
    }
}
