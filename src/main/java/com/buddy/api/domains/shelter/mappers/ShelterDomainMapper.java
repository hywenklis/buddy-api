package com.buddy.api.domains.shelter.mappers;

import com.buddy.api.domains.shelter.dtos.ShelterDto;
import com.buddy.api.domains.shelter.entities.ShelterEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShelterDomainMapper {

    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    ShelterEntity mapToEntity(ShelterDto shelterDto);

    ShelterDto mapToDto(ShelterEntity savedShelter);
}
