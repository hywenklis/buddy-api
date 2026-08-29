package com.buddy.api.web.pets.v2.mappers;

import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.PetV2ImageDto;
import com.buddy.api.web.pets.v2.responses.PetV2ImageResponse;
import com.buddy.api.web.pets.v2.responses.PetV2Response;
import com.buddy.api.web.pets.v2.responses.PetV2SummaryResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PetV2ResponseMapper {

    PetV2Response toResponse(PetV2Dto dto);

    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "guardianProfileId", source = "dto.guardianProfileId")
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "species", source = "dto.species")
    @Mapping(target = "gender", source = "dto.gender")
    @Mapping(target = "approximateAge", source = "dto.approximateAge")
    @Mapping(target = "size", source = "dto.size")
    @Mapping(target = "weight", source = "dto.weight")
    @Mapping(target = "isNeutered", source = "dto.isNeutered")
    @Mapping(target = "isForAdoption", source = "dto.isForAdoption")
    @Mapping(target = "coverImageUrl", source = "dto.coverImageUrl")
    @Mapping(target = "creationDate", source = "dto.creationDate")
    PetV2SummaryResponse toSummaryResponse(PetV2Dto dto);

    PetV2ImageResponse toImageResponse(PetV2ImageDto dto);
}
