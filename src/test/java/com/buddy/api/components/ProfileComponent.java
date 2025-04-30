package com.buddy.api.components;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static com.buddy.api.builders.profile.ProfileBuilder.profileEntity;
import static com.buddy.api.builders.profile.ProfileBuilder.profileRequest;

import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.web.profiles.requests.ProfileRequest;
import org.springframework.stereotype.Component;

@Component
public class ProfileComponent {
    private final AccountRepository accountRepository;

    public ProfileComponent(final AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public ProfileRequest.ProfileRequestBuilder validProfileRequest() {
        var accountEntity = accountRepository.save(validAccountEntity().build());
        return profileRequest().accountId(accountEntity.getAccountId());
    }

    public ProfileEntity.ProfileEntityBuilder validProfileEntity() {
        var accountEntity = accountRepository.save(validAccountEntity().build());
        return profileEntity().account(accountEntity);
    }
}
