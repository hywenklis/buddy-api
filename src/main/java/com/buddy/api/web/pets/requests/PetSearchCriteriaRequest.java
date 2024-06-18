package com.buddy.api.web.pets.requests;

import brave.internal.Nullable;
import java.time.LocalDate;
import java.util.UUID;

@Nullable
public record PetSearchCriteriaRequest(UUID id,
                                       UUID shelterId,
                                       String name,
                                       String specie,
                                       String sex,
                                       Integer age,
                                       LocalDate birthDate,
                                       String location,
                                       Double weight,
                                       String description) {
}
