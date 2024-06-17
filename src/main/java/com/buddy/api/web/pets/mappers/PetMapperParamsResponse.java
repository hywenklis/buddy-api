package com.buddy.api.web.pets.mappers;

import com.buddy.api.domains.pet.dtos.PetSearchCriteriaDto;
import com.buddy.api.web.pets.responses.PetParamsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PetMapperParamsResponse {

    @Mapping(target = "shelterResponseCompact.nameShelter", source = "shelterCompactDto.nameShelter")
    @Mapping(target = "shelterResponseCompact.avatar", source = "shelterCompactDto.avatar")
    PetParamsResponse mapToParamsResponse(PetSearchCriteriaDto petSearchCriteriaDto);
}
