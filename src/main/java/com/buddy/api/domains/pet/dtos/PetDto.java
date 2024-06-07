package com.buddy.api.domains.pet.dtos;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PetDto(UUID id,
                     UUID shelterId,
                     String name,
                     String specie,
                     String sex,
                     Integer age,
                     Double weight,
                     String description,
                     String avatar,
                     List<PetImageDto> images) {

    public PetDto {
        images = images == null ? List.of() : List.copyOf(images);
    }

    @Override
    public List<PetImageDto> images() {
        return Collections.unmodifiableList(this.images);
    }

    public static class PetDtoBuilder {
        public void images(List<PetImageDto> images) {
            this.images = images == null ? List.of() : List.copyOf(images);
        }
    }
}
