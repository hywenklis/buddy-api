package com.buddy.api.web.profiles.mappers;

import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.web.profiles.requests.ProfileRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ProfileMapperRequest {

    @Mapping(target = "isDeleted", ignore = true)
    ProfileDto toProfileDto(ProfileRequest profileRequest);
}