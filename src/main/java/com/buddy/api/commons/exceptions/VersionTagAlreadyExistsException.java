package com.buddy.api.commons.exceptions;

import java.io.Serial;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class VersionTagAlreadyExistsException extends DomainException {

    @Serial
    private static final long serialVersionUID = -5912672597296610996L;

    public VersionTagAlreadyExistsException(final String versionTag) {
        super("Version tag already exists: " + versionTag, "versionTag", HttpStatus.CONFLICT, null);
    }
}
