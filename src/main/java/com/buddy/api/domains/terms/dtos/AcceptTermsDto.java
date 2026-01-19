package com.buddy.api.domains.terms.dtos;

import lombok.Builder;

@Builder
public record AcceptTermsDto(
    String email,
    String ipAddress,
    String userAgent
) {}
