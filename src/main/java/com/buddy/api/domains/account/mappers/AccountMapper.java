package com.buddy.api.domains.account.mappers;

import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.entities.AccountEntity;
import java.util.UUID;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AccountMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "accountId", expression = "java(accountId)")
    AccountEntity toAccountEntity(final UUID accountId);

    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "isVerified", constant = "false")
    @Mapping(target = "isBlocked", constant = "false")
    @Mapping(target = "isDeleted", constant = "false")
    AccountEntity toAccountEntity(final AccountDto accountDto);

    AccountDto toAccountDto(final AccountEntity accountEntity);
}
