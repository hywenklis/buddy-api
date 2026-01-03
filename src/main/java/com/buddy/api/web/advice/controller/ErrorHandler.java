package com.buddy.api.web.advice.controller;

import com.buddy.api.commons.exceptions.DomainException;
import com.buddy.api.commons.exceptions.PetSearchException;
import com.buddy.api.web.advice.error.ErrorDetails;
import com.buddy.api.web.advice.error.ErrorResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
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
        return buildErrorResponse(ex.getFieldName(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
        final AccessDeniedException ex) {
        return buildErrorResponse(
            "authorization",
            "Access denied you are not allowed to perform this action",
            HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleSpringSecurityAuthException(
        final AuthenticationException ex) {
        return buildErrorResponse(
            "credentials",
            "authentication failed",
            HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
        final DomainException ex
    ) {
        return buildErrorResponse(ex.getFieldName(), ex.getMessage(), ex.getHttpStatus());
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

    private ResponseEntity<ErrorResponse> buildErrorResponse(
        final String field,
        final String message,
        final HttpStatus status
    ) {
        ErrorDetails error = new ErrorDetails(
            field,
            message,
            status,
            status.value(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(new ErrorResponse(List.of(error)));
    }
}
