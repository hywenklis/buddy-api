package com.buddy.api.domains.pet.mappers;

import com.buddy.api.domains.pet.dtos.PetDto;
import com.buddy.api.domains.pet.dtos.PetImageDto;
import com.buddy.api.domains.pet.dtos.PetSearchCriteriaDto;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.pet.entities.PetImageEntity;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

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

    @Mapping(target = "shelterId", source = "petEntity", qualifiedByName = "extractShelterId")
    @Mapping(target = "shelterCompactDto.nameShelter",
            source = "petEntity",
            qualifiedByName = "extractShelterName"
    )
    @Mapping(target = "shelterCompactDto.avatar",
            source = "petEntity",
            qualifiedByName = "extractShelterAvatar"
    )
    PetSearchCriteriaDto mapParamsToDto(PetEntity petEntity);

    @Named("extractShelterId")
    default UUID extractShelterId(PetEntity petEntity) {
        if (petEntity != null && petEntity.getShelter() != null) {
            return petEntity.getShelter().getId();
        }
        return null;
    }

    @Named("extractShelterName")
    default String extractShelterName(PetEntity petEntity) {
        return petEntity != null ? petEntity.getShelter().getNameShelter() : null;
    }

    @Named("extractShelterAvatar")
    default String extractShelterAvatar(PetEntity petEntity) {
        return petEntity != null ? petEntity.getShelter().getAvatar() : null;
    }
}
