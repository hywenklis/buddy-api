package com.buddy.api.web.shelter.mappers;

import com.buddy.api.domains.shelter.dtos.ShelterDto;
import com.buddy.api.web.shelter.requests.ShelterRequest;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ShelterMapperRequest {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @BeforeMapping
    default void ignorePets(ShelterRequest shelterRequest,
                            @MappingTarget ShelterDto.ShelterDtoBuilder shelterDtoBuilder) {
        shelterDtoBuilder.pets(null);
    }

    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    ShelterDto mapToDto(ShelterRequest shelterRequest);
}
