package com.buddy.api.units.domains.services.impl;

import static com.buddy.api.builders.account.AccountBuilder.validAccountDto;
import static com.buddy.api.builders.terms.TermsBuilder.validCreateTermsVersionDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.commons.exceptions.VersionTagAlreadyExistsException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.terms.dtos.CreateTermsVersionDto;
import com.buddy.api.domains.terms.dtos.TermsVersionDto;
import com.buddy.api.domains.terms.entities.TermsVersionEntity;
import com.buddy.api.domains.terms.mappers.TermsMapper;
import com.buddy.api.domains.terms.repositories.TermsVersionRepository;
import com.buddy.api.domains.terms.services.ActivateTermsVersion;
import com.buddy.api.domains.terms.services.FindTermsVersion;
import com.buddy.api.domains.terms.services.impl.CreateTermsVersionImpl;
import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

class CreateTermsVersionImplTest extends UnitTestAbstract {

    @Mock
    private TermsVersionRepository termsVersionRepository;

    @Mock
    private FindAccount findAccount;

    @Mock
    private FindTermsVersion findTermsVersion;

    @Mock
    private ActivateTermsVersion activateTermsVersion;

    @Spy
    private TermsMapper termsMapper = Mappers.getMapper(TermsMapper.class);

    @InjectMocks
    private CreateTermsVersionImpl createTermsVersion;

    @Nested
    @DisplayName("Success Scenarios")
    class SuccessScenarios {

        @Test
        @DisplayName("Should create version as draft and call activate when isActive is true")
        void should_create_version_when_valid_input_and_active() {
            final String adminEmail = RandomEmailUtils.generateValidEmail();
            final CreateTermsVersionDto dto = validCreateTermsVersionDto()
                .publishedByAccountEmail(adminEmail)
                .isActive(true)
                .build();

            final AccountDto publisherDto = validAccountDto()
                .email(new EmailAddress(adminEmail))
                .build();

            final TermsVersionEntity entityToSave = TermsVersionEntity.builder()
                .versionTag(dto.versionTag())
                .content(dto.content())
                .isActive(false)
                .publicationDate(LocalDate.now())
                .publishedBy(AccountEntity.builder().accountId(publisherDto.accountId()).build())
                .build();

            final TermsVersionEntity savedEntity = TermsVersionEntity.builder()
                .termsVersionId(UUID.randomUUID())
                .versionTag(dto.versionTag())
                .content(dto.content())
                .isActive(false)
                .publicationDate(LocalDate.now())
                .publishedBy(AccountEntity.builder().accountId(publisherDto.accountId()).build())
                .build();

            when(findTermsVersion.findByTag(dto.versionTag())).thenReturn(Optional.empty());
            when(findAccount.findByEmail(adminEmail)).thenReturn(publisherDto);
            when(termsVersionRepository.save(entityToSave)).thenReturn(savedEntity);

            final var result = createTermsVersion.create(dto);

            assertThat(result).isNotNull();
            assertThat(result.versionTag()).isEqualTo(dto.versionTag());
            assertThat(result.isActive()).isTrue();

            verify(findTermsVersion, times(1)).findByTag(dto.versionTag());
            verify(findAccount, times(1)).findByEmail(adminEmail);
            verify(termsVersionRepository, times(1)).save(entityToSave);

            verify(activateTermsVersion, times(1)).activate(savedEntity.getTermsVersionId());
        }

        @Test
        @DisplayName("Should create version without calling activate when isActive is false")
        void should_create_version_without_deactivating_when_inactive() {
            final String adminEmail = RandomEmailUtils.generateValidEmail();
            final CreateTermsVersionDto dto = validCreateTermsVersionDto()
                .publishedByAccountEmail(adminEmail)
                .isActive(false)
                .build();

            final AccountDto publisherDto = validAccountDto()
                .email(new EmailAddress(adminEmail))
                .build();

            final TermsVersionEntity entityToSave = TermsVersionEntity.builder()
                .versionTag(dto.versionTag())
                .content(dto.content())
                .isActive(false)
                .publicationDate(LocalDate.now())
                .publishedBy(AccountEntity.builder().accountId(publisherDto.accountId()).build())
                .build();

            final TermsVersionEntity savedEntity = TermsVersionEntity.builder()
                .termsVersionId(UUID.randomUUID())
                .versionTag(dto.versionTag())
                .content(dto.content())
                .isActive(false)
                .publicationDate(LocalDate.now())
                .publishedBy(AccountEntity.builder().accountId(publisherDto.accountId()).build())
                .build();

            when(findTermsVersion.findByTag(dto.versionTag())).thenReturn(Optional.empty());
            when(findAccount.findByEmail(adminEmail)).thenReturn(publisherDto);
            when(termsVersionRepository.save(entityToSave)).thenReturn(savedEntity);

            final var result = createTermsVersion.create(dto);

            assertThat(result).isNotNull();
            assertThat(result.isActive()).isFalse();

            verify(termsVersionRepository, times(1)).save(entityToSave);
            verify(activateTermsVersion, never()).activate(any());
        }
    }

    @Nested
    @DisplayName("Error Scenarios")
    class ErrorScenarios {

        @Test
        @DisplayName("Should throw VersionTagAlreadyExistsException when version tag exists")
        void should_throw_exception_when_version_tag_exists() {
            final CreateTermsVersionDto dto = validCreateTermsVersionDto().build();

            final TermsVersionDto existingDto = TermsVersionDto.builder()
                .termsVersionId(UUID.randomUUID())
                .versionTag(dto.versionTag())
                .build();

            when(findTermsVersion.findByTag(dto.versionTag())).thenReturn(Optional.of(existingDto));

            assertThatThrownBy(() -> createTermsVersion.create(dto))
                .isInstanceOf(VersionTagAlreadyExistsException.class)
                .hasMessage("Version tag already exists: " + dto.versionTag());

            verify(findTermsVersion, times(1)).findByTag(dto.versionTag());
            verify(findAccount, never()).findByEmail(dto.publishedByAccountEmail());

            verify(termsVersionRepository, never()).save(any(TermsVersionEntity.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException when publisher account not found")
        void should_throw_exception_when_publisher_not_found() {
            final String invalidEmail = RandomEmailUtils.generateValidEmail();

            final CreateTermsVersionDto dto = validCreateTermsVersionDto()
                .publishedByAccountEmail(invalidEmail)
                .build();

            when(findTermsVersion.findByTag(dto.versionTag())).thenReturn(Optional.empty());
            when(findAccount.findByEmail(invalidEmail))
                .thenThrow(new NotFoundException("account", "Account not found"));

            assertThatThrownBy(() -> createTermsVersion.create(dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Account not found");

            verify(findTermsVersion, times(1)).findByTag(dto.versionTag());
            verify(findAccount, times(1)).findByEmail(invalidEmail);
        }
    }
}
