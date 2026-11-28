package com.buddy.api.web.pets.v2.mappers;

import com.buddy.api.domains.pet.dtos.v2.CreatePetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.PetV2SearchCriteriaDto;
import com.buddy.api.domains.pet.dtos.v2.UpdatePetV2Dto;
import com.buddy.api.web.pets.v2.requests.CreatePetV2Request;
import com.buddy.api.web.pets.v2.requests.PetV2SearchCriteriaRequest;
import com.buddy.api.web.pets.v2.requests.UpdatePetV2Request;
import java.util.UUID;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PetV2RequestMapper {

    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "species", source = "request.species")
    @Mapping(target = "gender", source = "request.gender")
    @Mapping(target = "approximateAge", source = "request.approximateAge")
    @Mapping(target = "size", source = "request.size")
    @Mapping(target = "weight", source = "request.weight")
    @Mapping(target = "isNeutered", source = "request.isNeutered")
    @Mapping(target = "isForAdoption", source = "request.isForAdoption")
    @Mapping(target = "description", source = "request.description")
    CreatePetV2Dto toCreateDto(CreatePetV2Request request, UUID accountId);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "species", source = "request.species")
    @Mapping(target = "gender", source = "request.gender")
    @Mapping(target = "approximateAge", source = "request.approximateAge")
    @Mapping(target = "size", source = "request.size")
    @Mapping(target = "weight", source = "request.weight")
    @Mapping(target = "isNeutered", source = "request.isNeutered")
    @Mapping(target = "isForAdoption", source = "request.isForAdoption")
    @Mapping(target = "description", source = "request.description")
    UpdatePetV2Dto toUpdateDto(UUID id, UpdatePetV2Request request);

    PetV2SearchCriteriaDto toSearchCriteriaDto(PetV2SearchCriteriaRequest request);
}
