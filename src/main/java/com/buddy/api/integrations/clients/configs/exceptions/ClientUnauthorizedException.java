package com.buddy.api.integrations.clients.configs.exceptions;

import java.io.Serial;
import lombok.Getter;

@Getter
public class ClientUnauthorizedException extends GenericClientException {
    @Serial
    private static final long serialVersionUID = 6669386549169812714L;

    public ClientUnauthorizedException(final String message, final String errorBody, final Integer httpStatusCode) {
        super(message, errorBody, httpStatusCode);
    }
}
