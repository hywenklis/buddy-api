package com.buddy.api.domains.terms.dtos;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record TermsVersionDto(
    UUID termsVersionId,
    String versionTag,
    String content,
    boolean isActive,
    LocalDate publicationDate
) implements Serializable { }
