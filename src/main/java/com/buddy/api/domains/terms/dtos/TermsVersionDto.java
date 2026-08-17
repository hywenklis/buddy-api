package com.buddy.api.domains.terms.dtos;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public record TermsVersionDto(
    UUID termsVersionId,
    String versionTag,
    String content,
    boolean isActive,
    LocalDate publicationDate
) implements Serializable { }
