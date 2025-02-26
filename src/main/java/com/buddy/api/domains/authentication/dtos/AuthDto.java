package com.buddy.api.domains.authentication.dtos;

import com.buddy.api.domains.profile.dtos.ProfileDto;
import java.util.List;

public record AuthDto(
    String email,
    String password,
    List<ProfileDto> profiles,
    String accessToken,
    String refreshToken
) { }
