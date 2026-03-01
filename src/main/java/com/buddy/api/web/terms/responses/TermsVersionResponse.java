package com.buddy.api.web.terms.responses;

import java.time.LocalDate;
import java.util.UUID;

public record TermsVersionResponse(
    UUID termsVersionId,
    String versionTag,
    String content,
    Boolean isActive,
    LocalDate publicationDate
) { }
