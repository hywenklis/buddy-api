package com.buddy.api.web.shelter.mappers;

import com.buddy.api.domains.shelter.dtos.ShelterDto;
import com.buddy.api.web.shelter.responses.ShelterResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShelterMapperResponse {
    default ShelterResponse mapToResponse() {
        return ShelterResponse.builder().message("successfully created").build();
    }
}
