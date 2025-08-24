package com.buddy.api.integrations.clients.configs.exceptions;

import java.io.Serial;
import lombok.Getter;

@Getter
public class GenericClientException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 8166263305403305952L;

    private final int status;
    private final String errorBody;

    public GenericClientException(final String message,
                                  final String errorBody,
                                  final int status
                                  ) {
        super(message);
        this.status = status;
        this.errorBody = errorBody;
    }
}
