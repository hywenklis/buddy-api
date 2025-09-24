package com.buddy.api.domains.account.repositories;

import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.valueobjects.EmailAddress;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    Boolean existsByEmail(final EmailAddress email);

    Optional<AccountEntity> findByEmail(final EmailAddress email);

    Boolean existsByAccountIdAndIsDeleted(final UUID accountId, final Boolean isDeleted);

    @Modifying
    @Query("UPDATE AccountEntity a SET a.lastLogin = :lastLogin WHERE a.accountId = :accountId"
        + " AND a.isBlocked = false AND a.isDeleted = false")
    int updateLastLogin(UUID accountId, LocalDateTime lastLogin);

    @Modifying
    @Query("UPDATE AccountEntity a SET a.isVerified = :isVerified WHERE a.accountId = :accountId"
        + " AND a.isBlocked = false AND a.isDeleted = false")
    int updateIsVerified(UUID accountId, Boolean isVerified);
}
