package com.buddy.api.web.advice.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record ErrorDetails(String field,
                           String message,
                           String httpStatus,
                           Integer errorCode,
                           @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                           LocalDateTime timestamp) {
}
