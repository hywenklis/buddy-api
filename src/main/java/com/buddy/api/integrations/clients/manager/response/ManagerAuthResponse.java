package com.buddy.api.integrations.clients.manager.response;

import lombok.Builder;

@Builder
public record ManagerAuthResponse(
    String token,
    Long expiresIn
) { }
