package com.buddy.api.web.shelter.advice.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ErrorDetails(String field,
                           String message,
                           HttpStatus httpStatus,
                           Integer errorCode,
                           @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                           LocalDateTime timestamp) {
}