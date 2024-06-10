package com.buddy.api.domains.pet.mappers;

import com.buddy.api.domains.pet.dtos.PetDto;
import com.buddy.api.domains.pet.dtos.PetImageDto;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.pet.entities.PetImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PetDomainMapper {

    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "shelter", ignore = true)
    PetEntity mapToEntity(PetDto petDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pet", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    PetImageEntity mapToEntity(PetImageDto petImageDto);

    @Mapping(target = "shelterId", source = "shelter.id")
    PetDto mapToDto(PetEntity petEntity);

    PetImageDto mapToDto(PetImageEntity petImageEntity);
}
