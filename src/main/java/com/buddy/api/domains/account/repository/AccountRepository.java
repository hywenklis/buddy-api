package com.buddy.api.domains.account.repository;

import com.buddy.api.domains.account.entities.AccountEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    Boolean existsByEmail(final String email);
}
