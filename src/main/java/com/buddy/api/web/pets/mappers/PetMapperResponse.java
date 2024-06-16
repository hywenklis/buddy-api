package com.buddy.api.web.pets.mappers;

import com.buddy.api.web.pets.responses.PetResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PetMapperResponse {
    default PetResponse mapToResponse() {
        return PetResponse.builder().message("successfully created").build();
    }
}
