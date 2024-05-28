package com.buddy.api.units.domains.services.impls;

import static com.buddy.api.builders.shelter.ShelterBuilder.createShelterDto;
import static com.buddy.api.builders.shelter.ShelterBuilder.createShelterEntity;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.domains.shelter.mappers.ShelterDomainMapper;
import com.buddy.api.domains.shelter.repositories.ShelterRepository;
import com.buddy.api.domains.shelter.services.impls.FindShelterImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class FindShelterTest extends UnitTestAbstract {

    @Mock
    private ShelterRepository shelterRepository;

    @Mock
    private ShelterDomainMapper shelterDomainMapper;

    @InjectMocks
    private FindShelterImpl findShelter;


    @Test
    @DisplayName("Should return shelter when found in the database by cpf responsible")
    void should_return_shelter_when_found_in_database_by_cpf_responsible() {

        // Given
        var cpfResponsible = randomAlphabetic(10);

        var shelterDto = createShelterDto(
                randomAlphabetic(10),
                cpfResponsible,
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                null);

        var shelterEntity = createShelterEntity(
                shelterDto.nameShelter(),
                shelterDto.nameResponsible(),
                shelterDto.cpfResponsible(),
                shelterDto.email(),
                shelterDto.address(),
                shelterDto.phoneNumber(),
                shelterDto.avatar(),
                null);

        when(shelterDomainMapper.mapToDto(shelterEntity)).thenReturn(shelterDto);
        when(shelterRepository.findShelterByCpfResponsible(cpfResponsible))
                .thenReturn(Optional.of(shelterEntity));

        // When
        var shelterByCpfResponsible = findShelter.findShelterByCpfResponsible(cpfResponsible);

        // Then
        assertThat(shelterByCpfResponsible).isPresent();
        assertThat(shelterByCpfResponsible.get().id()).isEqualTo(shelterDto.id());

        assertThat(shelterByCpfResponsible.get().nameShelter())
                .isEqualTo(shelterDto.nameShelter());

        assertThat(shelterByCpfResponsible.get().nameResponsible())
                .isEqualTo(shelterDto.nameResponsible());

        assertThat(shelterByCpfResponsible.get().cpfResponsible())
                .isEqualTo(shelterDto.cpfResponsible());

        assertThat(shelterByCpfResponsible.get().email()).isEqualTo(shelterDto.email());
        assertThat(shelterByCpfResponsible.get().address()).isEqualTo(shelterDto.address());
        assertThat(shelterByCpfResponsible.get().phoneNumber()).isEqualTo(shelterDto.phoneNumber());
        assertThat(shelterByCpfResponsible.get().avatar()).isEqualTo(shelterDto.avatar());
        assertThat(shelterByCpfResponsible.get().pets()).isEqualTo(shelterDto.pets());

        // Verify
        verify(shelterRepository, times(1)).findShelterByCpfResponsible(cpfResponsible);
    }

    @Test
    @DisplayName("Should return shelter when found in the database by email")
    void should_return_shelter_when_found_in_database_by_email() {

        // Given
        var email = randomAlphabetic(10);

        var shelterDto = createShelterDto(
                randomAlphabetic(10),
                email,
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                null);

        var shelterEntity = createShelterEntity(
                shelterDto.nameShelter(),
                shelterDto.nameResponsible(),
                shelterDto.cpfResponsible(),
                shelterDto.email(),
                shelterDto.address(),
                shelterDto.phoneNumber(),
                shelterDto.avatar(),
                null);

        when(shelterDomainMapper.mapToDto(shelterEntity)).thenReturn(shelterDto);
        when(shelterRepository.findByEmail(email)).thenReturn(Optional.of(shelterEntity));

        // When
        var shelterByEmail = findShelter.findShelterByEmail(email);

        // Then
        assertThat(shelterByEmail).isPresent();
        assertThat(shelterByEmail.get().id()).isEqualTo(shelterDto.id());

        assertThat(shelterByEmail.get().nameShelter())
                .isEqualTo(shelterDto.nameShelter());

        assertThat(shelterByEmail.get().nameResponsible())
                .isEqualTo(shelterDto.nameResponsible());

        assertThat(shelterByEmail.get().cpfResponsible())
                .isEqualTo(shelterDto.cpfResponsible());

        assertThat(shelterByEmail.get().email()).isEqualTo(shelterDto.email());
        assertThat(shelterByEmail.get().address()).isEqualTo(shelterDto.address());
        assertThat(shelterByEmail.get().phoneNumber()).isEqualTo(shelterDto.phoneNumber());
        assertThat(shelterByEmail.get().avatar()).isEqualTo(shelterDto.avatar());
        assertThat(shelterByEmail.get().pets()).isEqualTo(shelterDto.pets());

        // Verify
        verify(shelterRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should return empty when shelter not found in the database by cpf responsible")
    void should_return_empty_when_shelter_not_found_in_database_by_cpf_responsible() {
        // Given
        var cpfResponsible = randomAlphabetic(10);

        when(shelterRepository.findShelterByCpfResponsible(cpfResponsible))
                .thenReturn(Optional.empty());

        // When
        var shelter = findShelter.findShelterByCpfResponsible(cpfResponsible);

        // Then
        assertThat(shelter).isEmpty();

        // Verify
        verify(shelterRepository, times(1)).findShelterByCpfResponsible(cpfResponsible);
    }

    @Test
    @DisplayName("Should return empty when shelter not found in the database by email")
    void should_return_empty_when_shelter_not_found_in_database_by_email() {
        // Given
        var email = randomAlphabetic(10);

        when(shelterRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        var shelter = findShelter.findShelterByEmail(email);

        // Then
        assertThat(shelter).isEmpty();

        // Verify
        verify(shelterRepository, times(1)).findByEmail(email);
    }
}
