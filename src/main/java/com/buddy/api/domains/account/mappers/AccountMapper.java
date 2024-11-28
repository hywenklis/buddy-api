package com.buddy.api.domains.account.mappers;

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
}
