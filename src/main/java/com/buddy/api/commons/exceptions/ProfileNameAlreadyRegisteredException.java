package com.buddy.api.commons.exceptions;

import java.io.Serial;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProfileNameAlreadyRegisteredException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1157465843980513596L;

    public ProfileNameAlreadyRegisteredException(final String profileName) {
        super(
            "Profile name already registered: " + profileName,
            "profileName",
            HttpStatus.CONFLICT
        );
    }
}
