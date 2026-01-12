package com.buddy.api.integrations.web.terms.controller;

import static com.buddy.api.builders.terms.TermsBuilder.validAcceptTermsDto;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectNotFoundFrom;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.integrations.IntegrationTestAbstract;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TermsControllerTest extends IntegrationTestAbstract {

    private static final String TERMS_ACTIVE_URL = TERMS_BASE_URL + "/active";
    private static final String TERMS_ACCEPT_URL = TERMS_BASE_URL + "/accept";

    @Nested
    @DisplayName("GET /v1/terms/active")
    class GetActiveTerms {

        @Test
        @DisplayName("Should return the active term content and cache the result")
        void should_return_active_terms() throws Exception {
            final var account = accountComponent.createAndAuthenticateUser().account();
            final var activeTerm = termsComponent.createActiveTerm(account);

            mockMvc.perform(get(TERMS_ACTIVE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versionTag").value(activeTerm.getVersionTag()))
                .andExpect(jsonPath("$.content").value(activeTerm.getContent()))
                .andExpect(jsonPath("$.publicationDate")
                    .value(activeTerm.getPublicationDate().toString()));

            assertThat(redisTemplate.hasKey("terms::active")).isTrue();

            mockMvc.perform(get(TERMS_ACTIVE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versionTag").value(activeTerm.getVersionTag()))
                .andExpect(jsonPath("$.content").value(activeTerm.getContent()))
                .andExpect(jsonPath("$.publicationDate")
                    .value(activeTerm.getPublicationDate().toString()));
        }

        @Test
        @DisplayName("Should return 404 when no active term exists")
        void should_return_not_found_when_no_active_term() throws Exception {
            expectNotFoundFrom(mockMvc.perform(get(TERMS_ACTIVE_URL)))
                .forField("terms", "No active terms found");
        }
    }

    @Nested
    @DisplayName("POST /v1/terms/accept")
    class AcceptTerms {

        @Test
        @DisplayName("Should accept terms successfully for an authenticated user")
        void should_accept_terms_successfully() throws Exception {
            final var validUser = accountComponent.createAndAuthenticateUser();
            final var activeTerm = termsComponent.createActiveTerm(validUser.account());
            final var expectedIp = "100.100.1.100";
            final var expectedUserAgent = "IntegrationTest/1.0";

            mockMvc.perform(post(TERMS_ACCEPT_URL)
                    .header(AUTHORIZATION, BEARER + validUser.jwt())
                    .header("User-Agent", expectedUserAgent)
                    .with(req -> {
                        req.setRemoteAddr(expectedIp);
                        return req;
                    })
                )
                .andExpect(status().isOk());

            final var savedAcceptance = termsAcceptanceRepository.findAll();

            assertThat(savedAcceptance)
                .as("Should have exactly one acceptance record")
                .singleElement()
                .satisfies(acceptance -> {
                    assertThat(acceptance.getTermsVersion().getTermsVersionId())
                        .isEqualTo(activeTerm.getTermsVersionId());

                    assertThat(acceptance.getAccount().getAccountId())
                        .isEqualTo(validUser.account().getAccountId());

                    assertThat(acceptance.getIpAddress())
                        .isEqualTo(expectedIp);

                    assertThat(acceptance.getUserAgent())
                        .isEqualTo(expectedUserAgent);

                    assertThat(acceptance.getAcceptedAt())
                        .isNotNull();
                });
        }

        @Test
        @DisplayName("Should return 404 if trying to accept terms but none are active")
        void should_fail_if_no_active_terms() throws Exception {
            final var user = accountComponent.createAndAuthenticateUser();
            final var request = validAcceptTermsDto()
                .email(user.account().getEmail().value())
                .build();

            expectNotFoundFrom(mockMvc.perform(post(TERMS_ACCEPT_URL)
                .header(AUTHORIZATION, BEARER + user.jwt())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
        }

        @Test
        @DisplayName("Should be idempotent: second acceptance should not create duplicate record")
        void should_be_idempotent() throws Exception {
            final var account = accountComponent.createAndAuthenticateUser().account();
            termsComponent.createActiveTerm(account);
            final var user = accountComponent.createAndAuthenticateUser();
            final var request = validAcceptTermsDto()
                .email(user.account().getEmail().value())
                .build();

            mockMvc.perform(post(TERMS_ACCEPT_URL)
                    .header(AUTHORIZATION, BEARER + user.jwt())
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            mockMvc.perform(post(TERMS_ACCEPT_URL)
                    .header(AUTHORIZATION, BEARER + user.jwt())
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            assertThat(termsAcceptanceRepository.count()).isEqualTo(1);
        }
    }
}
