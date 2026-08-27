package com.buddy.api.integrations.web.account.controller;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.domains.account.entities.AccountBlockReasonEntity;
import com.buddy.api.domains.account.repositories.AccountBlockReasonRepository;
import com.buddy.api.integrations.IntegrationTestAbstract;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("AccountBlockReasonEntity — column mapping")
class AccountBlockReasonEntityTest extends IntegrationTestAbstract {

    @Autowired
    private AccountBlockReasonRepository accountBlockReasonRepository;

    @Test
    @DisplayName("Should persist and retrieve deletionDate via deletion_date column mapping")
    void should_persist_and_retrieve_deletion_date() {
        // Arrange
        final var account = validAccountEntity().build();
        accountRepository.save(account);

        final var expectedDeletionDate = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

        final var blockReason = AccountBlockReasonEntity.builder()
            .account(account)
            .reason("Account deleted by user request.")
            .blockDate(LocalDateTime.of(2025, 12, 1, 9, 0, 0))
            .deletionDate(expectedDeletionDate)
            .build();

        // Act
        final var saved = accountBlockReasonRepository.save(blockReason);
        final var found = accountBlockReasonRepository.findById(saved.getAccountBlockReasonId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getDeletionDate())
            .isNotNull()
            .isEqualTo(expectedDeletionDate);
    }

    @Test
    @DisplayName("Should persist AccountBlockReasonEntity with null deletionDate when not provided")
    void should_persist_with_null_deletion_date() {
        // Arrange
        final var account = validAccountEntity().build();
        accountRepository.save(account);

        final var blockReason = AccountBlockReasonEntity.builder()
            .account(account)
            .reason("Suspicious activity detected.")
            .blockDate(LocalDateTime.of(2025, 11, 20, 8, 0, 0))
            .deletionDate(null)
            .build();

        // Act
        final var saved = accountBlockReasonRepository.save(blockReason);
        final var found = accountBlockReasonRepository.findById(saved.getAccountBlockReasonId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getDeletionDate()).isNull();
    }
}
