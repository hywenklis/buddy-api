package com.buddy.api.web.advice.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

public record ErrorDetails(String field,
                           String message,
                           HttpStatus httpStatus,
                           Integer errorCode,
                           @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                           LocalDateTime timestamp) {
}
