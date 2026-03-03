package com.buddy.api.domains.terms.dtos;

import lombok.Builder;

@Builder
public record CreateTermsVersionDto(
    String versionTag,
    String content,
    Boolean isActive,
    String publishedByAccountEmail
) { }
