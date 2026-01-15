package com.buddy.api.domains.terms.mappers;

import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.terms.dtos.TermsAcceptanceDto;
import com.buddy.api.domains.terms.dtos.TermsVersionDto;
import com.buddy.api.domains.terms.entities.TermsAcceptanceEntity;
import com.buddy.api.domains.terms.entities.TermsVersionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TermsMapper {
    TermsVersionDto toTermsVersionDto(final TermsVersionEntity termsVersionEntity);

    @Mapping(target = "publishedBy", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    TermsVersionEntity toTermsVersionEntity(final TermsVersionDto termsVersionDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", source = "account", qualifiedByName = "accountDtoToEntity")
    @Mapping(
        target = "termsVersion", source = "termsVersion", qualifiedByName = "versionDtoToEntity"
    )
    TermsAcceptanceEntity toTermsAcceptanceEntity(final TermsAcceptanceDto termsAcceptanceDto);

    @Named("accountDtoToEntity")
    default AccountEntity mapAccount(final AccountDto dto) {
        if (dto == null) {
            return null;
        }
        return AccountEntity.builder()
            .accountId(dto.accountId())
            .build();
    }

    @Named("versionDtoToEntity")
    default TermsVersionEntity mapVersion(final TermsVersionDto dto) {
        if (dto == null) {
            return null;
        }
        return TermsVersionEntity.builder()
            .termsVersionId(dto.termsVersionId())
            .build();
    }
}
