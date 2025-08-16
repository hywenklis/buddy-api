package com.buddy.api.commons.configurations.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "buddy.email")
@Builder
public record EmailProperties(
    @NotNull(message = "templates is required") @Valid Templates templates
) {
    public record Templates(
        @NotBlank(message = "from is required") String from,
        @NotBlank(message = "subject is required") String subject,
        @NotBlank(message = "templatePath is required") String templatePath,
        @NotBlank(message = "url is required") String url
    ) {}
}
