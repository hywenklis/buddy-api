package com.buddy.api.integrations.clients.manager.request;

import lombok.Builder;

@Builder(toBuilder = true)
public record ManagerAuthRequest(
    String username,
    String password,
    boolean encryptedPassword
) { }
