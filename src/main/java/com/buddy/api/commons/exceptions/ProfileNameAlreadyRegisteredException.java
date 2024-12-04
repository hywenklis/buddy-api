package com.buddy.api.commons.exceptions;

import java.io.Serial;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProfileNameAlreadyRegisteredException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1157465843980513596L;

    public ProfileNameAlreadyRegisteredException(final String message) {
        super(message, "name", HttpStatus.BAD_REQUEST);
    }
}
