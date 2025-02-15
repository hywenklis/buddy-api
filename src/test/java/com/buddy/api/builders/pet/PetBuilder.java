package com.buddy.api.builders.pet;

import static com.buddy.api.builders.pet.PetImageBuilder.createPetImageDto;
import static com.buddy.api.builders.pet.PetImageBuilder.createPetImageEntityCompleted;

import com.buddy.api.builders.shelter.ShelterBuilder;
import com.buddy.api.domains.pet.dtos.PetDto;
import com.buddy.api.domains.pet.dtos.PetImageDto;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.pet.entities.PetImageEntity;
import com.buddy.api.domains.shelter.entities.ShelterEntity;
import com.buddy.api.web.pets.requests.PetImageRequest;
import com.buddy.api.web.pets.requests.PetRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;

public class PetBuilder {

    public static PetRequest createPetRequest(final String name,
                                              final String specie,
                                              final String gender,
                                              final Double weight,
                                              final String description,
                                              final String avatar,
                                              final List<PetImageRequest> images,
                                              final UUID shelterId
    ) {
        return PetRequest.builder()
            .name(name)
            .specie(specie)
            .gender(gender)
            .birthDate(LocalDate.now())
            .location(RandomStringUtils.secure().nextAlphabetic(10))
            .weight(weight)
            .description(description)
            .avatar(avatar)
            .images(images)
            .shelterId(shelterId)
            .build();
    }

    public static PetRequest createPetRequest(final UUID shelterId) {
        return PetRequest.builder()
            .name(RandomStringUtils.secure().nextAlphabetic(10))
            .specie(RandomStringUtils.secure().nextAlphabetic(10))
            .gender(RandomStringUtils.secure().nextAlphabetic(10))
            .birthDate(LocalDate.now())
            .location(RandomStringUtils.secure().nextAlphabetic(10))
            .weight(Double.valueOf(RandomStringUtils.secure().nextNumeric(1)))
            .description(RandomStringUtils.secure().nextAlphabetic(10))
            .avatar(RandomStringUtils.secure().nextAlphabetic(10))
            .images(List.of(PetImageBuilder.createPetRequest()))
            .shelterId(shelterId)
            .build();
    }

    public static PetDto createPetDto(final UUID id,
                                      final UUID shelterId,
                                      final String name,
                                      final String specie,
                                      final String gender,
                                      final Integer age,
                                      final Double weight,
                                      final String description,
                                      final String avatar,
                                      final List<PetImageDto> images) {
        return PetDto.builder()
            .id(id)
            .shelterId(shelterId)
            .name(name)
            .specie(specie)
            .gender(gender)
            .birthDate(LocalDate.now())
            .location(RandomStringUtils.secure().nextAlphabetic(10))
            .avatar(avatar)
            .weight(weight)
            .description(description)
            .images(images)
            .build();
    }

    public static PetDto createPetDto(final UUID shelterId) {
        return PetDto.builder()
            .shelterId(shelterId)
            .name(RandomStringUtils.secure().nextAlphabetic(10))
            .specie(RandomStringUtils.secure().nextAlphabetic(10))
            .gender(RandomStringUtils.secure().nextAlphabetic(10))
            .birthDate(LocalDate.now())
            .location(RandomStringUtils.secure().nextAlphabetic(10))
            .avatar(RandomStringUtils.secure().nextAlphabetic(10))
            .weight(Double.valueOf(RandomStringUtils.secure().nextNumeric(2)))
            .description(RandomStringUtils.secure().nextAlphabetic(10))
            .images(List.of(createPetImageDto(RandomStringUtils.secure().nextAlphabetic(10))))
            .build();
    }

    public static PetEntity createPetEntity(final String name,
                                            final String specie,
                                            final String gender,
                                            final Double weight,
                                            final String description,
                                            final String avatar,
                                            final List<PetImageEntity> images,
                                            final ShelterEntity shelter) {
        return PetEntity.builder()
            .name(name)
            .specie(specie)
            .gender(gender)
            .birthDate(LocalDate.now())
            .location(RandomStringUtils.secure().nextAlphabetic(10))
            .weight(weight)
            .description(description)
            .avatar(avatar)
            .images(images)
            .shelter(shelter)
            .build();
    }

    public static PetEntity createPetEntity() {
        return PetEntity.builder()
            .name(RandomStringUtils.secure().nextAlphabetic(10))
            .specie(RandomStringUtils.secure().nextAlphabetic(10))
            .gender(RandomStringUtils.secure().nextAlphabetic(10))
            .birthDate(LocalDate.now())
            .location(RandomStringUtils.secure().nextAlphabetic(10))
            .weight(Double.valueOf(RandomStringUtils.secure().nextNumeric(2)))
            .description(RandomStringUtils.secure().nextAlphabetic(10))
            .avatar(RandomStringUtils.secure().nextAlphabetic(10))
            .images(List.of(createPetImageEntityCompleted()))
            .shelter(ShelterBuilder.createShelterEntity())
            .build();
    }

    public static PetEntity createPetEntity(final PetDto petDto,
                                            final ShelterEntity shelterEntity,
                                            final PetImageEntity petImageEntity
    ) {
        return PetEntity.builder()
            .id(petDto.id())
            .name(petDto.name())
            .specie(petDto.specie())
            .gender(petDto.gender())
            .birthDate(LocalDate.now())
            .location(RandomStringUtils.secure().nextAlphabetic(10))
            .weight(petDto.weight())
            .description(petDto.description())
            .avatar(petDto.avatar())
            .shelter(shelterEntity)
            .images(List.of(petImageEntity))
            .build();
    }

    public static PetEntity createPetEntity(final PetDto petDto,
                                            final ShelterEntity shelterEntity,
                                            final List<PetImageEntity> petImageEntity
    ) {
        return PetEntity.builder()
            .id(petDto.id())
            .name(petDto.name())
            .specie(petDto.specie())
            .gender(petDto.gender())
            .birthDate(LocalDate.now())
            .location(RandomStringUtils.secure().nextAlphabetic(10))
            .weight(petDto.weight())
            .description(petDto.description())
            .avatar(petDto.avatar())
            .shelter(shelterEntity)
            .images(petImageEntity)
            .build();
    }

    public static PetEntity createPetEntityWithoutImages(final PetDto petDto,
                                                         final ShelterEntity shelterEntity
    ) {
        return PetEntity.builder()
            .id(petDto.id())
            .name(petDto.name())
            .specie(petDto.specie())
            .gender(petDto.gender())
            .birthDate(LocalDate.now())
            .location(RandomStringUtils.secure().nextAlphabetic(10))
            .weight(petDto.weight())
            .description(petDto.description())
            .avatar(petDto.avatar())
            .shelter(shelterEntity)
            .build();
    }

    public static PetDto createPetDtoWithImages(final UUID shelterId) {
        return PetDto.builder()
            .name(RandomStringUtils.secure().nextAlphabetic(10))
            .specie(RandomStringUtils.secure().nextAlphabetic(10))
            .gender(RandomStringUtils.secure().nextAlphabetic(10))
            .birthDate(LocalDate.now())
            .location(RandomStringUtils.secure().nextAlphabetic(10))
            .weight(Double.valueOf(RandomStringUtils.secure().nextNumeric(2)))
            .description(RandomStringUtils.secure().nextAlphabetic(10))
            .avatar(RandomStringUtils.secure().nextAlphabetic(10))
            .shelterId(shelterId)
            .images(List.of(
                createPetImageDto(RandomStringUtils.secure().nextAlphabetic(10)),
                createPetImageDto(RandomStringUtils.secure().nextAlphabetic(10)))
            )
            .build();
    }

    public static PetDto createPetDtoWithoutImages(final UUID shelterId) {
        return PetDto.builder()
            .name(RandomStringUtils.secure().nextAlphabetic(10))
            .specie(RandomStringUtils.secure().nextAlphabetic(10))
            .gender(RandomStringUtils.secure().nextAlphabetic(10))
            .birthDate(LocalDate.now())
            .location(RandomStringUtils.secure().nextAlphabetic(10))
            .weight(Double.valueOf(RandomStringUtils.secure().nextNumeric(2)))
            .description(RandomStringUtils.secure().nextAlphabetic(10))
            .avatar(RandomStringUtils.secure().nextAlphabetic(10))
            .shelterId(shelterId)
            .images(List.of())
            .build();
    }
}
