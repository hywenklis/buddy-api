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
    @NotBlank(message = "from is required") String from,
    @NotNull(message = "templates is required") @Valid Templates templates
) {
    public record Templates(
        @NotNull(message = "verification is required") @Valid TemplateWithUrl verification,
        @NotNull(message = "forgotPassword is required") @Valid TemplateWithUrl forgotPassword,
        @NotNull(message = "passwordChanged is required") @Valid Template passwordChanged
    ) {
    }

    public record Template(
        @NotBlank(message = "subject is required") String subject,
        @NotBlank(message = "templatePath is required") String templatePath
    ) {
    }

    public record TemplateWithUrl(
        @NotBlank(message = "subject is required") String subject,
        @NotBlank(message = "templatePath is required") String templatePath,
        @NotBlank(message = "url is required") String url
    ) {
    }
}
