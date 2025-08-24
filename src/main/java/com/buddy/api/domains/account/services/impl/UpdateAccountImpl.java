package com.buddy.api.domains.account.services.impl;

import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.account.mappers.AccountMapper;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.account.services.UpdateAccount;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateAccountImpl implements UpdateAccount {

    private final AccountRepository accountRepository;
    private final FindAccount findAccount;
    private final AccountMapper accountMapper;

    @Override
    @Transactional
    public void updateLastLogin(final String email, final LocalDateTime lastLogin) {
        AccountDto accountDto = findAccount.findByEmail(email);
        AccountEntity account = accountMapper.toAccountEntityForUpdate(accountDto);
        account.setLastLogin(lastLogin);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void updateIsVerified(final String email, final Boolean isVerified) {
        AccountDto accountDto = findAccount.findByEmail(email);
        AccountEntity account = accountMapper.toAccountEntityForUpdate(accountDto);
        account.setIsVerified(isVerified);
        accountRepository.save(account);
    }
}