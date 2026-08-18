package com.buddy.api.commons.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidCurrentPasswordException extends DomainException {

    public InvalidCurrentPasswordException() {
        super("Invalid current password", "currentPassword", HttpStatus.BAD_REQUEST, null);
    }
}
