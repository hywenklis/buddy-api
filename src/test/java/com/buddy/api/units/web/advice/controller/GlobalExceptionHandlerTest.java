package com.buddy.api.units.web.advice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.exceptions.DomainException;
import com.buddy.api.commons.exceptions.PetSearchException;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.web.advice.controller.GlobalExceptionHandler;
import com.buddy.api.web.advice.error.ErrorResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest extends UnitTestAbstract {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void handleValidationErrors() {
        MethodArgumentNotValidException mockException = mock(MethodArgumentNotValidException.class);
        BindingResult mockBindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "field", "default message");
        when(mockException.getBindingResult()).thenReturn(mockBindingResult);
        when(mockBindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response =
            globalExceptionHandler.handleValidationErrors(mockException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errors().get(0).message())
            .isEqualTo("default message");
        assertThat(response.getBody().errors().get(0).field()).isEqualTo("field");
    }

    @Test
    @DisplayName("Should handle PetSearchException")
    void handlePetSearchException() {
        PetSearchException ex = new PetSearchException("query", "error happened");

        ResponseEntity<ErrorResponse> response =
            globalExceptionHandler.handlePetSearchException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errors().get(0).message()).isEqualTo(ex.getMessage());
        assertThat(response.getBody().errors().get(0).field()).isEqualTo("query");
    }

    @Test
    @DisplayName("Should handle AccessDeniedException")
    void handleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied manually");

        ResponseEntity<ErrorResponse> response =
            globalExceptionHandler.handleAccessDeniedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().errors().get(0).message())
            .isEqualTo("Access denied you are not allowed to perform this action");
        assertThat(response.getBody().errors().get(0).field()).isEqualTo("authorization");
    }

    @Test
    @DisplayName("Should handle AuthenticationException returning exactly Invalid Credentials")
    void handleAuthenticationException() {
        AuthenticationException authEx = new BadCredentialsException("Bad password");

        ResponseEntity<ErrorResponse> response =
            globalExceptionHandler.handleSpringSecurityAuthException(authEx);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().errors().get(0).message())
            .isEqualTo("Invalid Credentials");
        assertThat(response.getBody().errors().get(0).field()).isEqualTo("credentials");
    }

    @Test
    @DisplayName("Should hide stack traces for generic Exceptions and return Internal Server Error")
    void handleUnexpectedException() {
        Exception genericEx = new RuntimeException("DB Connection failed with secret 1234");

        ResponseEntity<ErrorResponse> response =
            globalExceptionHandler.handleUnexpectedException(genericEx);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        // The exact message must be "Internal Server Error"
        assertThat(response.getBody().errors().get(0).message()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().errors().get(0).field()).isEqualTo("server");
    }

    @Test
    @DisplayName("Should handle DomainException and propagate its native HTTP status")
    void handleDomainException() {
        DomainException ex = new DomainException(
            "Some error", "Some field", HttpStatus.CONFLICT, new RuntimeException()) {};

        ResponseEntity<ErrorResponse> response =
            globalExceptionHandler.handleDomainException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().errors().get(0).message()).isEqualTo("Some error");
        assertThat(response.getBody().errors().get(0).field()).isEqualTo("Some field");
    }
}
