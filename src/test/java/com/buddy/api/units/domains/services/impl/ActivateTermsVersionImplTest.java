package com.buddy.api.units.domains.services.impl;

import static com.buddy.api.builders.terms.TermsBuilder.validTermsVersionDto;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.terms.dtos.TermsVersionDto;
import com.buddy.api.domains.terms.repositories.TermsVersionRepository;
import com.buddy.api.domains.terms.services.FindTermsVersion;
import com.buddy.api.domains.terms.services.impl.ActivateTermsVersionImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class ActivateTermsVersionImplTest extends UnitTestAbstract {

    @Mock
    private TermsVersionRepository termsVersionRepository;

    @Mock
    private FindTermsVersion findTermsVersion;

    @InjectMocks
    private ActivateTermsVersionImpl activateTermsVersion;

    @Nested
    @DisplayName("Success Scenarios")
    class SuccessScenarios {

        @Test
        @DisplayName("Should activate an inactive terms version and deactivate all others")
        void should_activate_inactive_version() {
            final UUID termsVersionId = UUID.randomUUID();

            final TermsVersionDto inactiveDto = validTermsVersionDto()
                .termsVersionId(termsVersionId)
                .isActive(false)
                .build();

            final TermsVersionDto activatedDto = validTermsVersionDto()
                .termsVersionId(termsVersionId)
                .versionTag(inactiveDto.versionTag())
                .content(inactiveDto.content())
                .publicationDate(inactiveDto.publicationDate())
                .isActive(true)
                .build();

            when(findTermsVersion.findById(termsVersionId))
                .thenReturn(inactiveDto)
                .thenReturn(activatedDto);
            when(termsVersionRepository.deactivateAllActive()).thenReturn(1);
            when(termsVersionRepository.activateById(termsVersionId)).thenReturn(1);

            activateTermsVersion.activate(termsVersionId);

            verify(findTermsVersion, times(1)).findById(termsVersionId);
            verify(termsVersionRepository, times(1)).deactivateAllActive();
            verify(termsVersionRepository, times(1)).activateById(termsVersionId);
        }

        @Test
        @DisplayName("Should return current state when version is already active (idempotent)")
        void should_return_current_state_when_already_active() {
            final UUID termsVersionId = UUID.randomUUID();

            final TermsVersionDto activeDto = validTermsVersionDto()
                .termsVersionId(termsVersionId)
                .isActive(true)
                .build();

            when(findTermsVersion.findById(termsVersionId))
                .thenReturn(activeDto);

            activateTermsVersion.activate(termsVersionId);

            verify(findTermsVersion, times(1)).findById(termsVersionId);
            verify(termsVersionRepository, never()).deactivateAllActive();
            verify(termsVersionRepository, never()).activateById(termsVersionId);
        }
    }

    @Nested
    @DisplayName("Error Scenarios")
    class ErrorScenarios {

        @Test
        @DisplayName("Should throw NotFoundException when terms version does not exist")
        void should_throw_not_found_when_id_does_not_exist() {
            final UUID nonExistentId = UUID.randomUUID();

            when(findTermsVersion.findById(nonExistentId))
                .thenThrow(new NotFoundException(
                    "termsVersionId",
                    "Terms version not found with id: " + nonExistentId));

            assertThatThrownBy(() -> activateTermsVersion.activate(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(
                    "Terms version not found with id: " + nonExistentId
                );

            verify(findTermsVersion, times(1)).findById(nonExistentId);
            verify(termsVersionRepository, never()).deactivateAllActive();
            verify(termsVersionRepository, never()).activateById(nonExistentId);
        }
    }
}
