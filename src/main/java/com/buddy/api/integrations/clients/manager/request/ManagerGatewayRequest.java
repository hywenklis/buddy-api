package com.buddy.api.integrations.clients.manager.request;

import lombok.Builder;

@Builder
public record ManagerGatewayRequest<T>(
    String appId,
    String path,
    String httpMethod,
    T content
) { }
