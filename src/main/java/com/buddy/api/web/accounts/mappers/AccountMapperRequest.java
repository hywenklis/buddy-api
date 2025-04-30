package com.buddy.api.web.accounts.mappers;

import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.web.accounts.requests.AccountRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AccountMapperRequest {

    @Mapping(target = "email",
        expression =
            "java(new com.buddy.api.domains.valueobjects.EmailAddress(accountRequest.email()))"
    )
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "isBlocked", constant = "false")
    @Mapping(target = "lastLogin", ignore = true)
    AccountDto toAccountDto(AccountRequest accountRequest);
}
