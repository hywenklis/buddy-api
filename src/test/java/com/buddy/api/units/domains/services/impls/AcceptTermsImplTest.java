package com.buddy.api.units.domains.services.impls;

import static com.buddy.api.builders.account.AccountBuilder.validAccountDto;
import static com.buddy.api.builders.terms.TermsBuilder.validAcceptTermsDto;
import static com.buddy.api.builders.terms.TermsBuilder.validTermsVersionDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.terms.dtos.TermsAcceptanceDto;
import com.buddy.api.domains.terms.services.CreateTermsAcceptance;
import com.buddy.api.domains.terms.services.FindTermsAcceptance;
import com.buddy.api.domains.terms.services.FindTermsVersion;
import com.buddy.api.domains.terms.services.impls.AcceptTermsImpl;
import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.units.UnitTestAbstract;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class AcceptTermsImplTest extends UnitTestAbstract {

    @Mock
    private FindAccount findAccount;

    @Mock
    private FindTermsVersion findTermsVersion;

    @Mock
    private CreateTermsAcceptance createTermsAcceptance;

    @Mock
    private FindTermsAcceptance findTermsAcceptance;

    @InjectMocks
    private AcceptTermsImpl acceptTerms;

    @Captor
    private ArgumentCaptor<TermsAcceptanceDto> acceptanceCaptor;

    @Test
    @DisplayName("Should create terms acceptance successfully "
        + "when user has not accepted active version yet")
    void should_create_terms_acceptance_successfully() {
        final var acceptTermsDto = validAcceptTermsDto().build();
        final var accountDto = validAccountDto()
            .email(new EmailAddress(acceptTermsDto.email())).build();

        final var termsVersionDto = validTermsVersionDto().build();

        when(findAccount.findByEmail(acceptTermsDto.email())).thenReturn(accountDto);
        when(findTermsVersion.findActive()).thenReturn(termsVersionDto);
        when(findTermsAcceptance.exists(
            accountDto.accountId(), termsVersionDto.termsVersionId())
        ).thenReturn(false);

        acceptTerms.accept(acceptTermsDto);

        verify(createTermsAcceptance).create(acceptanceCaptor.capture());

        final var capturedAcceptance = acceptanceCaptor.getValue();

        assertThat(capturedAcceptance)
            .isNotNull()
            .satisfies(acceptance -> {
                assertThat(acceptance.account()).isEqualTo(accountDto);
                assertThat(acceptance.termsVersion()).isEqualTo(termsVersionDto);
                assertThat(acceptance.ipAddress()).isEqualTo(acceptTermsDto.ipAddress());
                assertThat(acceptance.userAgent()).isEqualTo(acceptTermsDto.userAgent());
            });
    }

    @Test
    @DisplayName("Should ignore and not create acceptance "
        + "when user already accepted the active version")
    void should_ignore_duplicate_acceptance() {
        final var acceptTermsDto = validAcceptTermsDto().build();
        final var accountDto = validAccountDto()
            .email(new EmailAddress(acceptTermsDto.email())).build();

        final var termsVersionDto = validTermsVersionDto().build();

        when(findAccount.findByEmail(acceptTermsDto.email())).thenReturn(accountDto);
        when(findTermsVersion.findActive()).thenReturn(termsVersionDto);
        when(findTermsAcceptance.exists(accountDto.accountId(), termsVersionDto.termsVersionId()))
            .thenReturn(true);

        acceptTerms.accept(acceptTermsDto);

        verify(createTermsAcceptance, never()).create(any());
        verify(findAccount).findByEmail(acceptTermsDto.email());
        verify(findTermsVersion).findActive();
    }
}
