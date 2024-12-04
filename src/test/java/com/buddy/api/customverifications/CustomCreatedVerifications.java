package com.buddy.api.customverifications;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.ResultActions;

public class CustomCreatedVerifications {
    public static void expectCreatedFrom(final ResultActions result)
        throws Exception {
        result.andExpectAll(
            status().isCreated(),
            jsonPath("$.message").value("successfully created")
        );
    }
}
