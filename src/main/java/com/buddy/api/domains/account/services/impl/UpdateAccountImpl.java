package com.buddy.api.domains.account.services.impl;

import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.account.services.UpdateAccount;
import com.buddy.api.domains.valueobjects.EmailAddress;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateAccountImpl implements UpdateAccount {

    private final AccountRepository accountRepository;

    @Transactional
    @Override
    public void updateLastLogin(final String email, final LocalDateTime lastLogin) {
        AccountEntity account = accountRepository.findByEmail(new EmailAddress(email))
            .orElseThrow(() -> new NotFoundException("accountId", "Account not found"));
        account.setLastLogin(lastLogin);
        accountRepository.save(account);
    }
}
