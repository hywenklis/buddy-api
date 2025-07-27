package com.buddy.api.integrations.clients.manager.response;

public record ManagerAuthResponse(
    String token,
    Integer expiresIn
) { }

