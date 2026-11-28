package com.buddy.api.units.commons.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.commons.exceptions.PetNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("PetNotFoundException — Unit Tests")
class PetNotFoundExceptionTest {

    @Test
    @DisplayName("Should construct with UUID")
    void should_construct_with_uuid() {
        final var id = UUID.randomUUID();
        final var ex = new PetNotFoundException(id);

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getFieldName()).isEqualTo("petId");
        assertThat(ex.getMessage()).isEqualTo("Pet with id '" + id + "' was not found.");
    }

    @Test
    @DisplayName("Should construct with custom message")
    void should_construct_with_message() {
        final var ex = new PetNotFoundException("Custom pet not found message");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getFieldName()).isEqualTo("petId");
        assertThat(ex.getMessage()).isEqualTo("Custom pet not found message");
    }
}
