package com.buddy.api.integrations.web.terms.controller;

import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectConflictFrom;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.terms.requests.CreateTermsVersionRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CreateTermsVersionControllerTest extends IntegrationTestAbstract {

    @Nested
    @DisplayName("POST /v1/terms - Success Scenarios")
    class SuccessScenarios {

        @Test
        @DisplayName("Should create an active terms version and return 201 Created")
        void should_create_active_terms_version() throws Exception {
            final var adminUser = accountComponent.createAndAuthenticateAdmin();

            final var request = CreateTermsVersionRequest.builder()
                .versionTag(RandomStringUtils.secure().nextAlphabetic(10))
                .content(RandomStringUtils.secure().nextAlphanumeric(10))
                .isActive(true)
                .build();

            mockMvc.perform(post(TERMS_BASE_URL)
                    .header(AUTHORIZATION, BEARER + adminUser.jwt())
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.termsVersionId").isNotEmpty())
                .andExpect(jsonPath("$.versionTag").value(request.versionTag()))
                .andExpect(jsonPath("$.content").value(request.content()))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.publicationDate").isNotEmpty());

            final var savedEntity = termsVersionRepository.findByVersionTag(request.versionTag());
            assertThat(savedEntity).isPresent();
            assertThat(savedEntity.get().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should create an inactive terms version as a draft")
        void should_create_inactive_terms_version() throws Exception {
            final var adminUser = accountComponent.createAndAuthenticateAdmin();

            final var request = CreateTermsVersionRequest.builder()
                .versionTag(RandomStringUtils.secure().nextAlphanumeric(10))
                .content(RandomStringUtils.secure().nextAlphanumeric(10))
                .isActive(false)
                .build();

            mockMvc.perform(post(TERMS_BASE_URL)
                    .header(AUTHORIZATION, BEARER + adminUser.jwt())
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isActive").value(false));

            final var savedEntity = termsVersionRepository.findByVersionTag(request.versionTag());
            assertThat(savedEntity).isPresent();
            assertThat(savedEntity.get().getIsActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("POST /v1/terms - Security & Authorization")
    class SecurityScenarios {

        @Test
        @DisplayName("Should return 403 Forbidden when no token is provided")
        void should_return_403_when_no_token() throws Exception {
            final var request = CreateTermsVersionRequest.builder()
                .versionTag(RandomStringUtils.secure().nextAlphanumeric(10))
                .content(RandomStringUtils.secure().nextAlphanumeric(10))
                .isActive(true)
                .build();

            mockMvc.perform(post(TERMS_BASE_URL)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when user is not ADMIN")
        void should_return_403_when_not_admin() throws Exception {
            final var regularUser = accountComponent.createAndAuthenticateUser();

            final var request = CreateTermsVersionRequest.builder()
                .versionTag(RandomStringUtils.secure().nextAlphanumeric(10))
                .content(RandomStringUtils.secure().nextAlphanumeric(10))
                .isActive(true)
                .build();

            mockMvc.perform(post(TERMS_BASE_URL)
                    .header(AUTHORIZATION, BEARER + regularUser.jwt())
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /v1/terms - Validation & Business Rules")
    class ValidationScenarios {

        @Test
        @DisplayName("Should return 400 Bad Request when mandatory fields are missing")
        void should_return_400_when_fields_are_missing() throws Exception {
            final var adminUser = accountComponent.createAndAuthenticateAdmin();

            final var invalidRequest = CreateTermsVersionRequest.builder()
                .versionTag("")
                .content("   ")
                .isActive(null)
                .build();

            expectBadRequestFrom(mockMvc.perform(post(TERMS_BASE_URL)
                .header(AUTHORIZATION, BEARER + adminUser.jwt())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))))
                .withTotalErrors(3)
                .forField("versionTag", "Version tag is mandatory")
                .forField("content", "Content is mandatory")
                .forField("isActive", "Is active flag is mandatory");
        }

        @Test
        @DisplayName("Should return 400 Bad Request "
            + "when version tag exceeds max length (Boundary Test)")
        void should_return_400_when_version_tag_is_too_long() throws Exception {
            final var adminUser = accountComponent.createAndAuthenticateAdmin();
            final String tooLongVersionTag = "v".repeat(51);

            final var invalidRequest = CreateTermsVersionRequest.builder()
                .versionTag(tooLongVersionTag)
                .content(RandomStringUtils.secure().nextAlphanumeric(10))
                .isActive(true)
                .build();

            expectBadRequestFrom(mockMvc.perform(post(TERMS_BASE_URL)
                .header(AUTHORIZATION, BEARER + adminUser.jwt())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))))
                .withTotalErrors(1)
                .forField("versionTag", "Version tag must be at most 50 characters");
        }

        @Test
        @DisplayName("Should return 409 Conflict when version tag already exists")
        void should_return_409_when_tag_exists() throws Exception {
            final var adminUser = accountComponent.createAndAuthenticateAdmin();
            final var versionTag = RandomStringUtils.secure().nextAlphanumeric(10);

            final var request = CreateTermsVersionRequest.builder()
                .versionTag(versionTag)
                .content(RandomStringUtils.secure().nextAlphanumeric(10))
                .isActive(false)
                .build();

            mockMvc.perform(post(TERMS_BASE_URL)
                    .header(AUTHORIZATION, BEARER + adminUser.jwt())
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            final var conflictingRequest = CreateTermsVersionRequest.builder()
                .versionTag(versionTag)
                .content(RandomStringUtils.secure().nextAlphanumeric(10))
                .isActive(true)
                .build();

            expectConflictFrom(mockMvc.perform(post(TERMS_BASE_URL)
                .header(AUTHORIZATION, BEARER + adminUser.jwt())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conflictingRequest))))
                .withTotalErrors(1)
                .forField("versionTag", "Version tag already exists: " + versionTag);
        }
    }
}
