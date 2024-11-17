package com.buddy.api.customverifications;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;

public class CustomErrorVerifications {
    private final ResultActions resultActions;

    public CustomErrorVerifications(final ResultActions resultActions) {
        this.resultActions = resultActions;
    }

    public static final String ERROR_FIELD_PATH = "$.errors[0].field";
    public static final String ERROR_MESSAGE_PATH = "$.errors[0].message";
    public static final String ERROR_HTTP_STATUS_PATH = "$.errors[0].httpStatus";
    public static final String ERROR_CODE_PATH = "$.errors[0].errorCode";
    public static final String ERROR_TIMESTAMP_PATH = "$.errors[0].timestamp";

    public static CustomErrorVerifications expectErrorStatusFrom(
        final ResultActions result,
        final HttpStatus status
    ) throws Exception {
        return new CustomErrorVerifications(result.andExpectAll(
            status().is(status.value()),
            jsonPath(ERROR_HTTP_STATUS_PATH).value(status.name()),
            jsonPath(ERROR_CODE_PATH).value(status.value()),
            jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty()
        ));
    }

    public static CustomErrorVerifications expectBadRequestFrom(final ResultActions result)
        throws Exception {
        return expectErrorStatusFrom(result, HttpStatus.BAD_REQUEST);
    }

    public static CustomErrorVerifications expectNotFoundFrom(final ResultActions result)
        throws Exception {
        return expectErrorStatusFrom(result, HttpStatus.NOT_FOUND);
    }

    public void forField(final String field, final String message) throws Exception {
        resultActions.andExpectAll(
            jsonPath(ERROR_FIELD_PATH).value(field),
            jsonPath(ERROR_MESSAGE_PATH).value(message)
        );
    }
}
