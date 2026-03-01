package com.buddy.api.web.terms.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateTermsVersionRequest(
    @NotBlank(message = "Version tag is mandatory")
    @Size(max = 50, message = "Version tag must be at most 50 characters")
    @Schema(description = "Unique version identifier", example = "v3.0")
    String versionTag,

    @NotBlank(message = "Content is mandatory")
    @Schema(description = "Full terms of use text")
    String content,

    @NotNull(message = "Is active flag is mandatory")
    @Schema(description = "Whether this version should be active immediately", example = "true")
    Boolean isActive
) { }
