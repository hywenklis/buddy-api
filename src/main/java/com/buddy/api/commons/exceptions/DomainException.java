package com.buddy.api.commons.exceptions;

import com.buddy.api.web.advice.error.ErrorDetails;
import java.io.Serial;
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
                           final String fieldName,
                           final HttpStatus httpStatus) {
        super(message);
        this.errors = Collections.singletonList(
            new ErrorDetails(fieldName, message, httpStatus, httpStatus.value(), null));
    }

    public DomainException(final List<ErrorDetails> errors) {
        super("Multiple validation errors occurred");
        this.errors = List.copyOf(errors);
    }

    public HttpStatus getHttpStatus() {
        return errors.isEmpty() ? HttpStatus.INTERNAL_SERVER_ERROR : errors.get(0).httpStatus();
    }
}
