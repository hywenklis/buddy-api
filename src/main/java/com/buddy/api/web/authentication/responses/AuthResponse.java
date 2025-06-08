package com.buddy.api.web.authentication.responses;

import java.util.List;

public record AuthResponse(List<ProfileResponse> profiles,
                           String accessToken,
                           String refreshToken
) { }
