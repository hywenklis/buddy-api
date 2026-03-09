package com.buddy.api.integrations.web.terms.controller;

import static com.buddy.api.customverifications.CustomErrorVerifications.expectNotFoundFrom;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.integrations.IntegrationTestAbstract;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ActivateTermsVersionControllerTest extends IntegrationTestAbstract {

    private static final String ACTIVATE_URL_TEMPLATE = TERMS_BASE_URL + "/%s/activate";

    @Nested
    @DisplayName("PATCH /v1/terms/{id}/activate - Success Scenarios")
    class SuccessScenarios {

        @Test
        @DisplayName("Should activate an inactive terms version and return 204 without body")
        void should_activate_inactive_terms_version() throws Exception {
            final var adminUser = accountComponent.createAndAuthenticateAdmin();
            final var inactiveTerms = termsComponent.createInactiveTerm(adminUser.account());

            final var activateUrl = String.format(ACTIVATE_URL_TEMPLATE,
                inactiveTerms.getTermsVersionId());

            mockMvc.perform(patch(activateUrl)
                    .header(AUTHORIZATION, BEARER + adminUser.jwt()))
                .andExpect(status().isNoContent());

            final var updatedEntity = termsVersionRepository
                .findById(inactiveTerms.getTermsVersionId());
            assertThat(updatedEntity).isPresent();
            assertThat(updatedEntity.get().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should deactivate previously active version when activating a new one")
        void should_deactivate_previous_active_version() throws Exception {
            final var adminUser = accountComponent.createAndAuthenticateAdmin();
            final var activeTerms = termsComponent.createActiveTerm(adminUser.account());
            final var inactiveTerms = termsComponent.createInactiveTerm(adminUser.account());

            final var activateUrl = String.format(ACTIVATE_URL_TEMPLATE,
                inactiveTerms.getTermsVersionId());

            mockMvc.perform(patch(activateUrl)
                    .header(AUTHORIZATION, BEARER + adminUser.jwt()))
                .andExpect(status().isNoContent());

            final var previouslyActive = termsVersionRepository
                .findById(activeTerms.getTermsVersionId());
            assertThat(previouslyActive).isPresent();
            assertThat(previouslyActive.get().getIsActive()).isFalse();

            final var newlyActive = termsVersionRepository
                .findById(inactiveTerms.getTermsVersionId());
            assertThat(newlyActive).isPresent();
            assertThat(newlyActive.get().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should return 204 no content when activating an already active version")
        void should_return_204_when_already_active() throws Exception {
            final var adminUser = accountComponent.createAndAuthenticateAdmin();
            final var activeTerms = termsComponent.createActiveTerm(adminUser.account());

            final var activateUrl = String.format(ACTIVATE_URL_TEMPLATE,
                activeTerms.getTermsVersionId());

            mockMvc.perform(patch(activateUrl)
                    .header(AUTHORIZATION, BEARER + adminUser.jwt()))
                .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("PATCH /v1/terms/{id}/activate - Security & Authorization")
    class SecurityScenarios {

        @Test
        @DisplayName("Should return 403 Forbidden when no token is provided")
        void should_return_403_when_no_token() throws Exception {
            final var activateUrl = String.format(ACTIVATE_URL_TEMPLATE, UUID.randomUUID());

            mockMvc.perform(patch(activateUrl))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when user is not ADMIN")
        void should_return_403_when_not_admin() throws Exception {
            final var regularUser = accountComponent.createAndAuthenticateUser();
            final var activateUrl = String.format(ACTIVATE_URL_TEMPLATE, UUID.randomUUID());

            mockMvc.perform(patch(activateUrl)
                    .header(AUTHORIZATION, BEARER + regularUser.jwt()))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /v1/terms/{id}/activate - Error Scenarios")
    class ErrorScenarios {

        @Test
        @DisplayName("Should return 404 Not Found when terms version does not exist")
        void should_return_404_when_id_not_found() throws Exception {
            final var adminUser = accountComponent.createAndAuthenticateAdmin();
            final var nonExistentId = UUID.randomUUID();
            final var activateUrl = String.format(ACTIVATE_URL_TEMPLATE, nonExistentId);

            expectNotFoundFrom(mockMvc.perform(patch(activateUrl)
                .header(AUTHORIZATION, BEARER + adminUser.jwt())))
                .withTotalErrors(1)
                .forField("termsVersionId",
                    "Terms version not found with id: " + nonExistentId);
        }
    }
}
