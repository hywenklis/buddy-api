package com.buddy.api.units.domains.services.impls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.domains.terms.repositories.TermsAcceptanceRepository;
import com.buddy.api.domains.terms.services.impls.FindTermsAcceptanceImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class FindTermsAcceptanceTest extends UnitTestAbstract {

    @Mock
    private TermsAcceptanceRepository termsAcceptanceRepository;

    @InjectMocks
    private FindTermsAcceptanceImpl findTermsAcceptance;

    @Test
    @DisplayName("Should return true when acceptance exists for given account and version")
    void should_return_true_when_acceptance_exists() {
        final var accountId = UUID.randomUUID();
        final var termsVersionId = UUID.randomUUID();

        when(termsAcceptanceRepository
            .existsByAccountAccountIdAndTermsVersionTermsVersionId(accountId, termsVersionId))
            .thenReturn(true);

        final var result = findTermsAcceptance.exists(accountId, termsVersionId);

        assertThat(result).isTrue();

        verify(termsAcceptanceRepository, times(1))
            .existsByAccountAccountIdAndTermsVersionTermsVersionId(accountId, termsVersionId);
    }

    @Test
    @DisplayName("Should return false when acceptance does not exist")
    void should_return_false_when_acceptance_does_not_exist() {
        final var accountId = UUID.randomUUID();
        final var termsVersionId = UUID.randomUUID();

        when(termsAcceptanceRepository
            .existsByAccountAccountIdAndTermsVersionTermsVersionId(accountId, termsVersionId))
            .thenReturn(false);

        final var result = findTermsAcceptance.exists(accountId, termsVersionId);

        assertThat(result).isFalse();

        verify(termsAcceptanceRepository, times(1))
            .existsByAccountAccountIdAndTermsVersionTermsVersionId(accountId, termsVersionId);
    }
}
