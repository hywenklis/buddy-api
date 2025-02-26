package com.buddy.api.web.authetication.responses;

import java.util.List;

public record AuthResponse(List<ProfileResponse> profiles,
                           String accessToken,
                           String refreshToken
) { }
