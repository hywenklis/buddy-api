package com.buddy.api.web.pets.requests;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PetRequest(
        String name,
        String specie,
        String sex,
        Integer age,
        Double weight,
        String description,
        String avatar,
        List<PetImageRequest> images,
        UUID shelterId
) {}
