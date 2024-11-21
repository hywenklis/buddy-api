package com.buddy.api.web.advice.controller;

import com.buddy.api.commons.exceptions.DomainException;
import com.buddy.api.commons.exceptions.PetSearchException;
import com.buddy.api.web.advice.error.ErrorDetails;
import com.buddy.api.web.advice.error.ErrorResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
        final MethodArgumentNotValidException ex) {
        List<ErrorDetails> errors = mapValidationErrors(ex.getBindingResult());
        return ResponseEntity.badRequest().body(new ErrorResponse(errors));
    }

    @ExceptionHandler(PetSearchException.class)
    public ResponseEntity<ErrorResponse> handlePetSearchException(final PetSearchException ex) {
        ErrorDetails error = new ErrorDetails(
            ex.getFieldName(),
            ex.getMessage(),
            HttpStatus.BAD_REQUEST,
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(List.of(error)));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
        final DomainException ex
    ) {
        ErrorDetails error = new ErrorDetails(
            ex.getFieldName(),
            ex.getMessage(),
            ex.getHttpStatus(),
            ex.getHttpStatus().value(),
            LocalDateTime.now());

        return ResponseEntity.status(ex.getHttpStatus())
            .body(new ErrorResponse(List.of(error)));
    }

    private List<ErrorDetails> mapValidationErrors(final BindingResult bindingResult) {
        return bindingResult.getFieldErrors()
            .stream()
            .map(fieldError -> new ErrorDetails(
                fieldError.getField(),
                fieldError.getDefaultMessage(),
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()))
            .toList();
    }
}
