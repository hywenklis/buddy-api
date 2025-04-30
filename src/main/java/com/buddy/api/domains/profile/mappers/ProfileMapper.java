package com.buddy.api.domains.profile.mappers;

import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ProfileMapper {

    @Mapping(target = "account", source = "account")
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    ProfileEntity toProfileEntity(ProfileDto profileDto, AccountEntity account);

    List<ProfileDto> toProfilesDto(List<ProfileEntity> profiles);
}

