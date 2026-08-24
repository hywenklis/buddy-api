package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class InvalidCurrentPasswordException extends DomainException {

    @Serial
    private static final long serialVersionUID = 7215162445100053745L;

    public InvalidCurrentPasswordException() {
        super("Invalid current password", "currentPassword", HttpStatus.BAD_REQUEST, null);
    }
}
