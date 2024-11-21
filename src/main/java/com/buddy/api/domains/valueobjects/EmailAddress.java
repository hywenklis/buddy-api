package com.buddy.api.domains.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Locale;

@Embeddable
public record EmailAddress(
    @Column(name = "email", nullable = false, unique = true)
    String value
) {
    public EmailAddress() {
        this("");
    }

    public EmailAddress {
        value = (value == null) ? "" : value.toLowerCase(Locale.ENGLISH);
    }
}
