package com.buddy.api.commons.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class BlockedOrDeletedAccountException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1072907289249447384L;

    public BlockedOrDeletedAccountException(final String fieldName, final String message) {
        super(fieldName, message, HttpStatus.FORBIDDEN);
    }
}
