package com.buddy.api.builders.pet;

import com.buddy.api.domains.pet.dtos.PetImageDto;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.pet.entities.PetImageEntity;
import com.buddy.api.web.pets.requests.PetImageRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;

public class PetImageBuilder {

    public static PetImageRequest createPetRequest(final String imageUrl) {
        return PetImageRequest.builder().imageUrl(imageUrl).build();
    }

    public static PetImageRequest createPetRequest() {
        return PetImageRequest
            .builder()
            .imageUrl(RandomStringUtils.secure().nextAlphabetic(10))
            .build();
    }

    public static PetImageDto createPetImageDto(final String imageUrl) {
        return PetImageDto.builder().imageUrl(imageUrl).build();
    }

    public static PetImageEntity createPetImageEntityCompleted() {
        return PetImageEntity.builder()
            .id(UUID.randomUUID())
            .imageUrl(RandomStringUtils.secure().nextAlphabetic(10))
            .pet(PetBuilder.createPetEntity())
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();
    }

    public static PetImageEntity createPetImageEntity(final String imageUrl,
                                                      final PetEntity petEntity) {
        return PetImageEntity.builder()
            .id(UUID.randomUUID())
            .imageUrl(imageUrl)
            .pet(petEntity)
            .build();
    }
}
