package com.buddy.api.units.domains.services.impls;

import static com.buddy.api.builders.terms.TermsBuilder.validTermsVersionEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.terms.entities.TermsVersionEntity;
import com.buddy.api.domains.terms.mappers.TermsMapper;
import com.buddy.api.domains.terms.repositories.TermsVersionRepository;
import com.buddy.api.domains.terms.services.impls.FindTermsVersionImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

class FindTermsVersionTest extends UnitTestAbstract {

    @Mock
    private TermsVersionRepository termsVersionRepository;

    @Spy
    private TermsMapper termsMapper = Mappers.getMapper(TermsMapper.class);

    @InjectMocks
    private FindTermsVersionImpl findTermsVersion;

    @Test
    @DisplayName("Should return the active terms version DTO when found in database")
    void should_return_active_terms_when_found() {
        final TermsVersionEntity entity = validTermsVersionEntity().build();

        when(termsVersionRepository.findFirstByIsActiveTrueOrderByPublicationDateDesc())
            .thenReturn(Optional.of(entity));

        final var result = findTermsVersion.findActive();

        verify(termsVersionRepository,
            times(1)).findFirstByIsActiveTrueOrderByPublicationDateDesc();
        verify(termsMapper, times(1)).toTermsVersionDto(entity);

        assertThat(result)
            .isNotNull()
            .satisfies(dto -> {
                assertThat(dto.termsVersionId()).isEqualTo(entity.getTermsVersionId());
                assertThat(dto.versionTag()).isEqualTo(entity.getVersionTag());
                assertThat(dto.content()).isEqualTo(entity.getContent());
                assertThat(dto.isActive()).isEqualTo(entity.getIsActive());
            });
    }

    @Test
    @DisplayName("Should throw NotFoundException when no active terms exist")
    void should_throw_exception_when_no_active_terms_found() {
        when(termsVersionRepository.findFirstByIsActiveTrueOrderByPublicationDateDesc())
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> findTermsVersion.findActive())
            .isInstanceOf(NotFoundException.class)
            .hasMessage("No active terms found")
            .hasFieldOrPropertyWithValue("fieldName", "terms");

        verify(termsVersionRepository,
            times(1)).findFirstByIsActiveTrueOrderByPublicationDateDesc();
    }
}
