package com.buddy.api.builders.shelter;

import com.buddy.api.domains.pet.dtos.PetDto;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.shelter.dtos.ShelterDto;
import com.buddy.api.domains.shelter.entities.ShelterEntity;
import com.buddy.api.web.shelter.requests.ShelterRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ShelterBuilder {

    public static ShelterRequest createShelterRequest(String nameShelter,
                                                       String nameResponsible,
                                                       String cpfResponsible,
                                                       String email
    ) {
        return ShelterRequest.builder()
                .nameShelter(nameShelter)
                .nameResponsible(nameResponsible)
                .cpfResponsible(cpfResponsible)
                .email(email)
                .build();
    }

    public static ShelterDto createShelterDto(String nameShelter,
                                               String nameResponsible,
                                               String cpfResponsible,
                                               String email,
                                               String address,
                                               String phoneNumber,
                                               String avatar,
                                               List<PetDto> pets) {
        return ShelterDto.builder()
                .nameShelter(nameShelter)
                .nameResponsible(nameResponsible)
                .cpfResponsible(cpfResponsible)
                .address(address)
                .phoneNumber(phoneNumber)
                .email(email)
                .avatar(avatar)
                .pets(pets)
                .build();
    }

    public static ShelterEntity createShelterEntity(String nameShelter,
                                                     String nameResponsible,
                                                     String cpfResponsible,
                                                     String email,
                                                     String address,
                                                     String phoneNumber,
                                                     String avatar,
                                                     List<PetEntity> pets) {
        return ShelterEntity.builder()
                .id(UUID.randomUUID())
                .nameShelter(nameShelter)
                .nameResponsible(nameResponsible)
                .cpfResponsible(cpfResponsible)
                .address(address)
                .phoneNumber(phoneNumber)
                .email(email)
                .avatar(avatar)
                .pets(pets)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }
}
