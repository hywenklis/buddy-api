package com.buddy.api.builders.pet;

import com.buddy.api.domains.pet.dtos.PetDto;
import com.buddy.api.domains.pet.dtos.PetImageDto;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.pet.entities.PetImageEntity;
import com.buddy.api.domains.shelter.entities.ShelterEntity;
import com.buddy.api.web.pets.requests.PetImageRequest;
import com.buddy.api.web.pets.requests.PetRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PetBuilder {

    public static PetRequest createPetRequest(String name,
                                                  String specie,
                                                  String sex,
                                                  Integer age,
                                                  Double weight,
                                                  String description,
                                                  String avatar,
                                                  List<PetImageRequest> images,
                                                  UUID shelterId
    ) {
        return PetRequest.builder()
                .name(name)
                .specie(specie)
                .sex(sex)
                .age(age)
                .weight(weight)
                .description(description)
                .avatar(avatar)
                .images(images)
                .shelterId(shelterId)
                .build();
    }

    public static PetDto createPetDto(UUID id,
                                          UUID shelterId,
                                          String name,
                                          String specie,
                                          String sex,
                                          Integer age,
                                          Double weight,
                                          String description,
                                          String avatar,
                                          List<PetImageDto> images) {
        return PetDto.builder()
                .id(id)
                .shelterId(shelterId)
                .name(name)
                .specie(specie)
                .sex(sex)
                .age(age)
                .avatar(avatar)
                .weight(weight)
                .description(description)
                .images(images)
                .build();
    }

    public static PetEntity createPetEntity(String name,
                                                String specie,
                                                String sex,
                                                Integer age,
                                                Double weight,
                                                String description,
                                                String avatar,
                                                List<PetImageEntity> images,
                                                ShelterEntity shelter) {
        return PetEntity.builder()
                .id(UUID.randomUUID())
                .name(name)
                .specie(specie)
                .sex(sex)
                .age(age)
                .weight(weight)
                .description(description)
                .avatar(avatar)
                .images(images)
                .shelter(shelter)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }
}
