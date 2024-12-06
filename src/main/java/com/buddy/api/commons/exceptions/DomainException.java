package com.buddy.api.commons.exceptions;

import com.buddy.api.web.advice.error.ErrorDetails;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DomainException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3389907679696634663L;

    private final List<ErrorDetails> errors;

    public DomainException(final String message,
                           final String field,
                           final HttpStatus httpStatus
    ) {
        super(message);
        this.errors = Collections.singletonList(new ErrorDetails(
            field,
            message,
            httpStatus,
            httpStatus.value(),
            LocalDateTime.now()
        ));
    }

    public DomainException(final List<ErrorDetails> errors) {
        super("Multiple validation errors occurred");
        this.errors = errors;
    }

    public HttpStatus getHttpStatus() {
        return errors.isEmpty() ? HttpStatus.INTERNAL_SERVER_ERROR : errors.get(0).httpStatus();
    }
}
