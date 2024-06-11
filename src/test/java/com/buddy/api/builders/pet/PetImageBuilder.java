package com.buddy.api.builders.pet;

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

    public static PetImageDto createPetDto(String imageUrl) {
        return PetImageDto.builder().imageUrl(imageUrl).build();
    }

    public static PetImageEntity createPetEntity(String imageUrl, PetEntity pet) {
        return PetImageEntity.builder()
                .id(UUID.randomUUID())
                .imageUrl(imageUrl)
                .pet(pet)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }
}
