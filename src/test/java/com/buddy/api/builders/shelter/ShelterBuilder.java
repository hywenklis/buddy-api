package com.buddy.api.builders.shelter;

import static com.buddy.api.utils.RandomCpfUtils.generateValidCpf;
import static com.buddy.api.utils.RandomEmailUtils.generateValidEmail;

import com.buddy.api.builders.pet.PetBuilder;
import com.buddy.api.domains.pet.dtos.PetDto;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.shelter.dtos.ShelterDto;
import com.buddy.api.domains.shelter.entities.ShelterEntity;
import com.buddy.api.web.shelter.requests.ShelterRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;

public class ShelterBuilder {

    public static ShelterRequest createShelterRequest(final String nameShelter,
                                                      final String nameResponsible,
                                                      final String cpfResponsible,
                                                      final String email,
                                                      final String avatar
    ) {
        return ShelterRequest.builder()
            .nameShelter(nameShelter)
            .nameResponsible(nameResponsible)
            .cpfResponsible(cpfResponsible)
            .email(email)
            .avatar(avatar)
            .build();
    }

    public static ShelterRequest createShelterRequest() {
        return ShelterRequest.builder()
            .nameShelter(RandomStringUtils.secure().nextAlphabetic(10))
            .nameResponsible(RandomStringUtils.secure().nextAlphabetic(10))
            .cpfResponsible(generateValidCpf())
            .email(generateValidEmail())
            .avatar(RandomStringUtils.secure().nextAlphabetic(10))
            .build();
    }

    public static ShelterDto createShelterDto(final String nameShelter,
                                              final String nameResponsible,
                                              final String cpfResponsible,
                                              final String email,
                                              final String address,
                                              final String phoneNumber,
                                              final String avatar,
                                              final List<PetDto> pets) {
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

    public static ShelterDto createShelterDto() {
        return ShelterDto.builder()
            .nameShelter(RandomStringUtils.secure().nextAlphabetic(10))
            .nameResponsible(RandomStringUtils.secure().nextAlphabetic(10))
            .cpfResponsible(RandomStringUtils.secure().nextAlphabetic(10))
            .address(RandomStringUtils.secure().nextAlphabetic(10))
            .phoneNumber(RandomStringUtils.secure().nextAlphabetic(10))
            .email(RandomStringUtils.secure().nextAlphabetic(10))
            .avatar(RandomStringUtils.secure().nextAlphabetic(10))
            .pets(List.of())
            .build();
    }

    public static ShelterEntity createShelterEntity(final String nameShelter,
                                                    final String nameResponsible,
                                                    final String cpfResponsible,
                                                    final String email,
                                                    final String address,
                                                    final String phoneNumber,
                                                    final String avatar,
                                                    final List<PetEntity> pets) {
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

    public static ShelterEntity createShelterEntity() {
        return ShelterEntity.builder()
            .id(UUID.randomUUID())
            .nameShelter(RandomStringUtils.secure().nextAlphabetic(10))
            .nameResponsible(RandomStringUtils.secure().nextAlphabetic(10))
            .cpfResponsible(generateValidCpf())
            .address(RandomStringUtils.secure().nextAlphabetic(10))
            .phoneNumber(RandomStringUtils.secure().nextAlphabetic(10))
            .email(generateValidEmail())
            .avatar(RandomStringUtils.secure().nextAlphabetic(10))
            .pets(List.of(PetBuilder.createPetEntity()))
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();
    }

    public static ShelterEntity createShelterEntity(final ShelterDto shelterDto) {
        return ShelterEntity.builder()
            .id(UUID.randomUUID())
            .nameShelter(shelterDto.nameShelter())
            .nameResponsible(shelterDto.nameResponsible())
            .cpfResponsible(shelterDto.cpfResponsible())
            .address(shelterDto.address())
            .phoneNumber(shelterDto.phoneNumber())
            .email(shelterDto.email())
            .avatar(shelterDto.avatar())
            .pets(List.of())
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();
    }

    public static ShelterEntity createShelterEntityNoPets() {
        return ShelterEntity.builder()
            .id(UUID.randomUUID())
            .nameShelter(RandomStringUtils.secure().nextAlphabetic(10))
            .nameResponsible(RandomStringUtils.secure().nextAlphabetic(10))
            .cpfResponsible(generateValidCpf())
            .address(RandomStringUtils.secure().nextAlphabetic(10))
            .phoneNumber(RandomStringUtils.secure().nextAlphabetic(10))
            .email(generateValidEmail())
            .avatar(RandomStringUtils.secure().nextAlphabetic(10))
            .pets(List.of())
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();
    }
}
