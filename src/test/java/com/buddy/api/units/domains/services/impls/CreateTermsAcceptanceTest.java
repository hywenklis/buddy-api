package com.buddy.api.units.domains.services.impls;

import static com.buddy.api.builders.terms.TermsBuilder.validTermsAcceptanceDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.buddy.api.domains.terms.dtos.TermsAcceptanceDto;
import com.buddy.api.domains.terms.entities.TermsAcceptanceEntity;
import com.buddy.api.domains.terms.mappers.TermsMapper;
import com.buddy.api.domains.terms.repositories.TermsAcceptanceRepository;
import com.buddy.api.domains.terms.services.impls.CreateTermsAcceptanceImpl;
import com.buddy.api.units.UnitTestAbstract;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

public class CreateTermsAcceptanceTest extends UnitTestAbstract {

    @Mock
    private TermsAcceptanceRepository termsAcceptanceRepository;

    @Spy
    private TermsMapper termsMapper = Mappers.getMapper(TermsMapper.class);

    @Captor
    private ArgumentCaptor<TermsAcceptanceEntity> entityCaptor;

    @InjectMocks
    private CreateTermsAcceptanceImpl createTermsAcceptance;

    @Test
    @DisplayName("Should convert DTO to Entity and save it successfully")
    void should_create_terms_acceptance_successfully() {
        final TermsAcceptanceDto acceptanceDto = validTermsAcceptanceDto().build();

        createTermsAcceptance.create(acceptanceDto);

        verify(termsMapper, times(1)).toTermsAcceptanceEntity(acceptanceDto);
        verify(termsAcceptanceRepository, times(1)).save(entityCaptor.capture());

        final TermsAcceptanceEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity)
            .isNotNull()
            .satisfies(entity -> {
                assertThat(entity.getIpAddress()).isEqualTo(acceptanceDto.ipAddress());
                assertThat(entity.getUserAgent()).isEqualTo(acceptanceDto.userAgent());
                assertThat(entity.getAccount().getAccountId())
                    .isEqualTo(acceptanceDto.account().accountId());
                assertThat(entity.getTermsVersion().getTermsVersionId())
                    .isEqualTo(acceptanceDto.termsVersion().termsVersionId());
            });
    }

    @Test
    @DisplayName("Should return null when mapping null "
        + "nested objects (Coverage for default methods)")
    void should_return_null_in_mapper_default_methods() {
        assertThat(termsMapper.mapAccount(null)).isNull();
        assertThat(termsMapper.mapVersion(null)).isNull();
    }
}
