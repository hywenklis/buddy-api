package com.buddy.api.domains.account.mappers;

import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.entities.AccountEntity;
import java.util.UUID;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "accountId", expression = "java(accountId)")
    AccountEntity toAccountEntity(final UUID accountId);

    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "isVerified", expression = "java(false)")
    @Mapping(target = "isBlocked", expression = "java(false)")
    @Mapping(target = "isDeleted", expression = "java(false)")
    AccountEntity toAccountEntity(final AccountDto accountDto);
}
