package com.buddy.api.integrations.clients.configs.exceptions;

import java.io.Serial;

public class ClientBadRequestException extends GenericClientException {
    @Serial
    private static final long serialVersionUID = 4903576513113759286L;

    public ClientBadRequestException(final String message, final String errorBody, final Integer httpStatusCode) {
        super(message, errorBody, httpStatusCode);
    }
}
