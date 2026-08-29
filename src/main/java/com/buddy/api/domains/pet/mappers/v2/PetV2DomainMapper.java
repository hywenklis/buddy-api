package com.buddy.api.domains.pet.mappers.v2;

import com.buddy.api.domains.image.entities.ImageEntity;
import com.buddy.api.domains.pet.dtos.v2.CreatePetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.PetV2ImageDto;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import java.util.Comparator;
import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PetV2DomainMapper {

    @Mapping(target = "petV2Id", ignore = true)
    @Mapping(target = "guardianProfile", source = "guardianProfile")
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "species", source = "dto.species")
    @Mapping(target = "gender", source = "dto.gender")
    @Mapping(target = "approximateAge", source = "dto.approximateAge")
    @Mapping(
        target = "ageReportDate",
        expression = "java(dto != null && dto.approximateAge() != null "
            + "? java.time.LocalDate.now() : null)"
    )
    @Mapping(target = "size", source = "dto.size")
    @Mapping(target = "weight", source = "dto.weight")
    @Mapping(target = "isNeutered", source = "dto.isNeutered")
    @Mapping(target = "isForAdoption", source = "dto.isForAdoption")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    PetV2Entity toEntity(CreatePetV2Dto dto, ProfileEntity guardianProfile);

    @Mapping(target = "id", source = "entity.petV2Id")
    @Mapping(target = "guardianProfileId", source = "entity.guardianProfile.profileId")
    @Mapping(
        target = "coverImageUrl",
        source = "images",
        qualifiedByName = "extractCoverImageUrl"
    )
    @Mapping(target = "images", source = "images", qualifiedByName = "mapImages")
    PetV2Dto toDto(PetV2Entity entity, List<ImageEntity> images);

    default PetV2Dto toDto(final PetV2Entity entity) {
        if (entity == null) {
            return null;
        }
        return toDto(entity, List.of());
    }

    @Named("extractCoverImageUrl")
    default String extractCoverImageUrl(final List<ImageEntity> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
            .min(Comparator.comparing(ImageEntity::getDisplayOrder,
                Comparator.nullsLast(Comparator.naturalOrder())))
            .map(ImageEntity::getFilePath)
            .orElse(null);
    }

    @Named("mapImages")
    default List<PetV2ImageDto> mapImages(final List<ImageEntity> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
            .sorted(Comparator.comparing(ImageEntity::getDisplayOrder,
                Comparator.nullsLast(Comparator.naturalOrder())))
            .map(img -> PetV2ImageDto.builder()
                .id(img.getImageId())
                .url(img.getFilePath())
                .displayOrder(img.getDisplayOrder())
                .build())
            .toList();
    }
}
