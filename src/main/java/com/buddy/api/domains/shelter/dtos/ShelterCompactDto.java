package com.buddy.api.domains.shelter.dtos;

import lombok.Builder;

@Builder
public record ShelterCompactDto(String nameShelter, String avatar) { }
