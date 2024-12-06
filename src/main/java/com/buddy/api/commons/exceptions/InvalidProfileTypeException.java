package com.buddy.api.commons.exceptions;

import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import java.io.Serial;
import org.springframework.http.HttpStatus;

public class InvalidProfileTypeException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 8182926263647923997L;

    public InvalidProfileTypeException(final ProfileTypeEnum profileTypeEnum) {
        super(
            "Profile type" + profileTypeEnum + "cannot be created by user",
            "profileType",
            HttpStatus.BAD_REQUEST
        );
    }
}
