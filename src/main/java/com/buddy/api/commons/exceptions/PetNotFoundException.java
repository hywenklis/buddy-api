package com.buddy.api.commons.exceptions;

import java.io.Serial;
import java.util.UUID;

public class PetNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PetNotFoundException(final UUID petId) {
        super("petId", "Pet with id '" + petId + "' was not found.");
    }

    public PetNotFoundException(final String message) {
        super("petId", message);
    }
}
