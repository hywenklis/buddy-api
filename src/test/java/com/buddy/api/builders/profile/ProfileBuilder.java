package com.buddy.api.builders.profile;

import static com.buddy.api.utils.RandomStringUtils.generateRandomString;

import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.web.profiles.requests.ProfileRequest;

public class ProfileBuilder {
    public static ProfileRequest.ProfileRequestBuilder profileRequest() {
        return ProfileRequest
            .builder()
            .name(generateRandomString(6))
            .description(generateRandomString(10))
            .bio(generateRandomString(10))
            .profileType(ProfileTypeEnum.USER);
    }

    public static ProfileDto.ProfileDtoBuilder profileDto() {
        return ProfileDto
            .builder()
            .name(generateRandomString(6))
            .description(generateRandomString(10))
            .bio(generateRandomString(10))
            .profileType(ProfileTypeEnum.USER);
    }
}
