package com.buddy.api.builders.pet;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

import com.buddy.api.domains.pet.dtos.PetImageDto;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.pet.entities.PetImageEntity;
import com.buddy.api.web.pets.requests.PetImageRequest;
import java.time.LocalDateTime;
import java.util.UUID;

public class PetImageBuilder {

    public static PetImageRequest createPetRequest(String imageUrl) {
        return PetImageRequest.builder().imageUrl(imageUrl).build();
    }

    public static PetImageRequest createPetRequest() {
        return PetImageRequest.builder().imageUrl(randomAlphabetic(10)).build();
    }

    public static PetImageDto createPetImageDto(String imageUrl) {
        return PetImageDto.builder().imageUrl(imageUrl).build();
    }

    public static PetImageEntity createPetImageEntityCompleted() {
        return PetImageEntity.builder()
                .id(UUID.randomUUID())
                .imageUrl(randomAlphabetic(10))
                .pet(PetBuilder.createPetEntity())
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }

    public static PetImageEntity createPetImageEntity(String imageUrl, PetEntity petEntity) {
        return PetImageEntity.builder()
                .id(UUID.randomUUID())
                .imageUrl(imageUrl)
                .pet(petEntity)
                .build();
    }
}
