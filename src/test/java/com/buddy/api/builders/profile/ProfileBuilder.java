package com.buddy.api.builders.profile;

import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.web.profiles.requests.ProfileRequest;
import org.apache.commons.lang3.RandomStringUtils;

public class ProfileBuilder {
    public static ProfileEntity.ProfileEntityBuilder profileEntity() {
        return ProfileEntity
            .builder()
            .name(RandomStringUtils.secure().nextAlphabetic(6))
            .description(RandomStringUtils.secure().nextAlphabetic(10))
            .bio(RandomStringUtils.secure().nextAlphabetic(10))
            .profileType(ProfileTypeEnum.USER)
            .isDeleted(false);
    }

    public static ProfileRequest.ProfileRequestBuilder profileRequest() {
        return ProfileRequest
            .builder()
            .name(RandomStringUtils.secure().nextAlphabetic(6))
            .description(RandomStringUtils.secure().nextAlphabetic(10))
            .bio(RandomStringUtils.secure().nextAlphabetic(10))
            .profileType(ProfileTypeEnum.USER);
    }

    public static ProfileDto.ProfileDtoBuilder profileDto() {
        return ProfileDto
            .builder()
            .name(RandomStringUtils.secure().nextAlphabetic(6))
            .description(RandomStringUtils.secure().nextAlphabetic(10))
            .bio(RandomStringUtils.secure().nextAlphabetic(10))
            .profileType(ProfileTypeEnum.USER);
    }
}
