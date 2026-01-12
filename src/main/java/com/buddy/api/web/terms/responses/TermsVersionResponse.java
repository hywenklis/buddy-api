package com.buddy.api.web.terms.responses;

import java.time.LocalDate;

public record TermsVersionResponse(
    String versionTag,
    String content,
    LocalDate publicationDate
) { }
