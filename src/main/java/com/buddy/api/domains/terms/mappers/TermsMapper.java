package com.buddy.api.domains.terms.mappers;

import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.terms.dtos.CreateTermsVersionDto;
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

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", source = "account", qualifiedByName = "accountDtoToEntity")
    @Mapping(
        target = "termsVersion", source = "termsVersion", qualifiedByName = "versionDtoToEntity"
    )
    TermsAcceptanceEntity toTermsAcceptanceEntity(final TermsAcceptanceDto termsAcceptanceDto);

    @Mapping(target = "termsVersionId", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "versionTag", source = "dto.versionTag")
    @Mapping(target = "content", source = "dto.content")
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "publishedBy", source = "publishedBy", qualifiedByName = "accountDtoToEntity")
    @Mapping(target = "publicationDate", expression = "java(java.time.LocalDate.now())")
    TermsVersionEntity toTermsVersionEntity(final CreateTermsVersionDto dto,
                                            final AccountDto publishedBy,
                                            final boolean isActive
    );

    @Mapping(target = "publishedBy", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    TermsVersionEntity toTermsVersionEntity(final TermsVersionDto termsVersionDto);


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
