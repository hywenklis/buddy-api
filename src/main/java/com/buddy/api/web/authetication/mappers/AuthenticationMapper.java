package com.buddy.api.web.authetication.mappers;

import com.buddy.api.domains.authentication.dtos.AuthDto;
import com.buddy.api.web.authetication.requests.AuthRequest;
import com.buddy.api.web.authetication.responses.AuthResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AuthenticationMapper {
    @Mapping(target = "profiles", ignore = true)
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    AuthDto toAuthDto(AuthRequest request);

    AuthResponse toAuthResponse(AuthDto authDto);
}
