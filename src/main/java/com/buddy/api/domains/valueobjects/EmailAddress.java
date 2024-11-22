package com.buddy.api.domains.valueobjects;

import com.buddy.api.commons.exceptions.InvalidEmailAddressException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Locale;

@Embeddable
public record EmailAddress(
    @Column(name = "email", nullable = false, unique = true)
    String value
) {
    private EmailAddress() {
        this("");
    }

    public EmailAddress {
        if (value == null || value.isEmpty()) {
            throw new InvalidEmailAddressException("Email address value cannot be null or empty");
        }
        value = value.toLowerCase(Locale.ENGLISH);
    }
}
