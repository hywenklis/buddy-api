package com.buddy.api.web.pets.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PetRequest(
        @NotBlank(message = "Name of mandatory pet")
        String name,

        @NotBlank(message = "Specie of mandatory pet")
        String specie,

        @NotBlank(message = "Sex of mandatory pet")
        String sex,

        @NotNull(message = "Age of mandatory pet")
        Integer age,

        @NotNull(message = "Weight of mandatory pet")
        Double weight,

        @NotBlank(message = "Description of mandatory pet")
        String description,

        String avatar,
        List<PetImageRequest> images,

        @NotBlank(message = "ShelterId of mandatory pet")
        UUID shelterId
) {}
