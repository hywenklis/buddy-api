package com.buddy.api.units.domains.services.impls;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.builders.shelter.ShelterBuilder;
import com.buddy.api.domains.shelter.entities.ShelterEntity;
import com.buddy.api.domains.shelter.mappers.ShelterDomainMapper;
import com.buddy.api.domains.shelter.repositories.ShelterRepository;
import com.buddy.api.domains.shelter.services.impls.CreateShelterImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class CreateShelterTest extends UnitTestAbstract {

    @Mock
    private ShelterRepository shelterRepository;

    @Mock
    private ShelterDomainMapper mapper;

    @InjectMocks
    private CreateShelterImpl createShelter;

    @Test
    @DisplayName("Should return success"
            + "when registering an shelter that does not exist in the database")
    void save_shelter_success() {

        // Given
        final var shelterDto = ShelterBuilder.createShelterDto(
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                List.of()
        );

        final var shelterEntity = ShelterBuilder.createShelterEntity(
                shelterDto.nameShelter(),
                shelterDto.nameResponsible(),
                shelterDto.cpfResponsible(),
                shelterDto.email(),
                shelterDto.address(),
                shelterDto.phoneNumber(),
                shelterDto.avatar(),
                List.of()
        );

        // Mock
        when(mapper.mapToEntity(shelterDto)).thenReturn(shelterEntity);
        when(mapper.mapToDto(shelterEntity)).thenReturn(shelterDto);
        when(shelterRepository.save(shelterEntity)).thenReturn(shelterEntity);

        // When
        createShelter.create(shelterDto);

        // Then
        ArgumentCaptor<ShelterEntity> shelterEntityCaptor = forClass(ShelterEntity.class);
        verify(shelterRepository, times(1)).save(shelterEntityCaptor.capture());

        ShelterEntity savedShelterEntity = shelterEntityCaptor.getValue();

        assertThat(shelterDto.nameShelter()).isEqualTo(savedShelterEntity.getNameShelter());
        assertThat(savedShelterEntity.getNameResponsible()).isEqualTo(shelterDto.nameResponsible());
        assertThat(savedShelterEntity.getCpfResponsible()).isEqualTo(shelterDto.cpfResponsible());
        assertThat(savedShelterEntity.getEmail()).isEqualTo(shelterDto.email());
        assertThat(savedShelterEntity.getAddress()).isEqualTo(shelterDto.address());
        assertThat(savedShelterEntity.getPhoneNumber()).isEqualTo(shelterDto.phoneNumber());

        verify(shelterRepository, times(1)).save(shelterEntityCaptor.capture());
    }
}
