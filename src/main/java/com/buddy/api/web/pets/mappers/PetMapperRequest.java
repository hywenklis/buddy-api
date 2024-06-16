package com.buddy.api.web.pets.mappers;

import com.buddy.api.domains.pet.dtos.PetDto;
import com.buddy.api.domains.pet.dtos.PetImageDto;
import com.buddy.api.web.pets.requests.PetImageRequest;
import com.buddy.api.web.pets.requests.PetRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PetMapperRequest {

    @Mapping(target = "id", ignore = true)
    PetDto mapToDto(PetRequest petRequest);

    PetImageDto mapToDto(PetImageRequest petImageRequest);
}
