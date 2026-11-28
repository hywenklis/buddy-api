package com.buddy.api.commons.exceptions;

import java.io.Serial;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class ActiveShelterProfileNotFoundException extends DomainException {

    @Serial
    private static final long serialVersionUID = 6312907159834517293L;

    public ActiveShelterProfileNotFoundException(final UUID accountId) {
        super(
            "Active shelter profile not found for account '" + accountId + "'.",
            "profile",
            HttpStatus.resolve(422),
            null
        );
    }
}
