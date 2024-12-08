package com.buddy.api.integrations.commons.validation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.integrations.IntegrationTestAbstract;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class ValidationCollectorTest extends IntegrationTestAbstract {

    private static final String API = "/test/validationcollection";

    @Test
    @DisplayName("should not conflict between requests")
    void should_not_affect_other_requests() throws Exception {
        mockMvc
            .perform(post(API + "/invalidrequest"))
            .andExpect(status().isOk());

        mockMvc
            .perform(get(API + "/throw"))
            .andExpect(status().isOk());
    }
}
