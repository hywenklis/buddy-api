package com.buddy.api.integrations.clients.configs.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class ClientServiceUnavailableException extends GenericClientException {
    @Serial
    private static final long serialVersionUID = -5682506791039192890L;

    public ClientServiceUnavailableException(final String message, final String errorBody) {
        super(message, errorBody, HttpStatus.SERVICE_UNAVAILABLE.value());
    }
}
