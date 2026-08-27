package com.buddy.api.domains.account.repositories;

import com.buddy.api.domains.account.entities.AccountBlockReasonEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountBlockReasonRepository
    extends JpaRepository<AccountBlockReasonEntity, UUID> {
}

