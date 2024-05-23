package com.buddy.api.web.shelter.mappers;

import com.buddy.api.domains.shelter.dtos.ShelterDto;
import com.buddy.api.web.shelter.requests.ShelterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShelterMapperRequest {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "address", ignore = true)
    ShelterDto mapToDto(ShelterRequest shelterRequest);
}
