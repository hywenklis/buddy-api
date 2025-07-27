package com.buddy.api.integrations.clients.configs.exceptions;

import java.io.Serial;

public class ClientNotFoundException extends GenericClientException {
    @Serial
    private static final long serialVersionUID = -4020737471454521988L;

    public ClientNotFoundException(final String message, final String errorBody, final Integer httpStatusCode) {
        super(message, errorBody, httpStatusCode);
    }
}
