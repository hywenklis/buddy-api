package com.buddy.api.integrations.commons.validation;

import com.buddy.api.commons.exceptions.DomainException;
import com.buddy.api.commons.validation.ValidationCollector;
import com.buddy.api.web.advice.error.ErrorDetails;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/validationcollection")
public class FakeValidationController {
    private final ValidationCollector validationCollector;

    public FakeValidationController(final ValidationCollector validationCollector) {
        this.validationCollector = validationCollector;
    }

    @PostMapping("/invalidrequest")
    @ResponseStatus(HttpStatus.OK)
    public void validateWithError() {
        this.validationCollector.validate(
            () -> true,
            new DomainException(
                List.of(
                    new ErrorDetails(
                        "someField",
                        "some message",
                        HttpStatus.BAD_REQUEST,
                        HttpStatus.BAD_REQUEST.value(),
                        LocalDateTime.now()
                    )
                )
            )
        );
    }

    @GetMapping("/throw")
    @ResponseStatus(HttpStatus.OK)
    public void getErrors() {
        validationCollector.throwIfErrors();
    }
}
