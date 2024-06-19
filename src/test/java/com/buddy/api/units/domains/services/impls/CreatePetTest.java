package com.buddy.api.units.domains.services.impls;

import static com.buddy.api.builders.pet.PetBuilder.createPetDto;
import static com.buddy.api.builders.pet.PetBuilder.createPetDtoWithImages;
import static com.buddy.api.builders.pet.PetBuilder.createPetDtoWithoutImages;
import static com.buddy.api.builders.pet.PetBuilder.createPetEntity;
import static com.buddy.api.builders.pet.PetBuilder.createPetEntityWithoutImages;
import static com.buddy.api.builders.pet.PetImageBuilder.createPetImageEntity;
import static com.buddy.api.builders.shelter.ShelterBuilder.createShelterDto;
import static com.buddy.api.builders.shelter.ShelterBuilder.createShelterEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.domains.exceptions.NotFoundException;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.pet.mappers.PetDomainMapper;
import com.buddy.api.domains.pet.repositories.PetRepository;
import com.buddy.api.domains.pet.services.impls.CreatePetImpl;
import com.buddy.api.domains.shelter.repositories.ShelterRepository;
import com.buddy.api.units.UnitTestAbstract;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class CreatePetTest extends UnitTestAbstract {

    @Mock
    private PetRepository petRepository;

    @Mock
    private ShelterRepository shelterRepository;

    @Mock
    private PetDomainMapper mapper;

    @InjectMocks
    private CreatePetImpl createPet;

    @Test
    @DisplayName("Should return success when registering a pet")
    void save_pet_success() {

        // Given
        final var shelterDto = createShelterDto();
        final var shelterEntity = createShelterEntity(shelterDto);

        shelterEntity.setPets(new ArrayList<>());

        final var petDto = createPetDto(shelterEntity.getId());
        final var petEntity = createPetEntityWithoutImages(petDto, shelterEntity);
        final var petImageEntity = createPetImageEntity(
                petDto.images().getFirst().imageUrl(),
                petEntity
        );

        petEntity.setImages(List.of(petImageEntity));

        // Mock
        when(mapper.mapToEntity(petDto)).thenReturn(petEntity);
        when(shelterRepository.findById(petDto.shelterId())).thenReturn(Optional.of(shelterEntity));
        when(petRepository.save(petEntity)).thenReturn(petEntity);

        // When
        createPet.create(petDto);

        // Then
        ArgumentCaptor<PetEntity> petEntityCaptor = forClass(PetEntity.class);
        verify(petRepository, times(1)).save(petEntityCaptor.capture());

        PetEntity savedPetEntity = petEntityCaptor.getValue();

        assertThat(savedPetEntity.getId()).isEqualTo(petEntity.getId());
        assertThat(savedPetEntity.getName()).isEqualTo(petEntity.getName());
        assertThat(savedPetEntity.getShelter()).isEqualTo(petEntity.getShelter());
        assertThat(savedPetEntity.getSpecie()).isEqualTo(petEntity.getSpecie());
        assertThat(savedPetEntity.getGender()).isEqualTo(petEntity.getGender());
        assertThat(savedPetEntity.getBirthDate()).isEqualTo(petEntity.getBirthDate());
        assertThat(savedPetEntity.getLocation()).isEqualTo(petEntity.getLocation());
        assertThat(savedPetEntity.getAvatar()).isEqualTo(petEntity.getAvatar());
        assertThat(savedPetEntity.getWeight()).isEqualTo(petEntity.getWeight());
        assertThat(savedPetEntity.getDescription()).isEqualTo(petEntity.getDescription());
        assertThat(savedPetEntity.getImages()).isEqualTo(petEntity.getImages());
    }

    @Test
    @DisplayName("Should throw NotFoundException when shelter is not found")
    void save_pet_shelter_not_found() {
        // Given
        final var petDto = createPetDto(UUID.randomUUID());

        // Mock
        when(shelterRepository.findById(petDto.shelterId())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> createPet.create(petDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Shelter not found");

        verify(petRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return success when registering a pet with no images")
    void save_pet_success_no_images() {

        // Given
        final var shelterDto = createShelterDto();
        final var shelterEntity = createShelterEntity(shelterDto);
        shelterEntity.setPets(new ArrayList<>());
        final var petDto = createPetDtoWithoutImages(shelterEntity.getId());
        final var petEntity = createPetEntityWithoutImages(petDto, shelterEntity);

        // Mock
        when(mapper.mapToEntity(petDto)).thenReturn(petEntity);
        when(shelterRepository.findById(petDto.shelterId())).thenReturn(Optional.of(shelterEntity));
        when(petRepository.save(petEntity)).thenReturn(petEntity);

        // When
        createPet.create(petDto);

        // Then
        ArgumentCaptor<PetEntity> petEntityCaptor = forClass(PetEntity.class);
        verify(petRepository, times(1)).save(petEntityCaptor.capture());

        PetEntity savedPetEntity = petEntityCaptor.getValue();
        assertThat(savedPetEntity.getId()).isEqualTo(petEntity.getId());
        assertThat(savedPetEntity.getName()).isEqualTo(petEntity.getName());
        assertThat(savedPetEntity.getShelter()).isEqualTo(petEntity.getShelter());
        assertThat(savedPetEntity.getSpecie()).isEqualTo(petEntity.getSpecie());
        assertThat(savedPetEntity.getGender()).isEqualTo(petEntity.getGender());
        assertThat(savedPetEntity.getBirthDate()).isEqualTo(petEntity.getBirthDate());
        assertThat(savedPetEntity.getLocation()).isEqualTo(petEntity.getLocation());
        assertThat(savedPetEntity.getAvatar()).isEqualTo(petEntity.getAvatar());
        assertThat(savedPetEntity.getWeight()).isEqualTo(petEntity.getWeight());
        assertThat(savedPetEntity.getDescription()).isEqualTo(petEntity.getDescription());
        assertThat(savedPetEntity.getImages()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Should return success when registering a pet with images")
    void save_pet_success_with_images() {

        // Given
        final var shelterDto = createShelterDto();
        final var shelterEntity = createShelterEntity(shelterDto);
        shelterEntity.setPets(new ArrayList<>());
        final var petDto = createPetDtoWithImages(shelterEntity.getId());
        final var petEntity = createPetEntity(petDto, shelterEntity, new ArrayList<>());

        final var petImageEntity1 = createPetImageEntity(
                petDto.images().get(0).imageUrl(),
                petEntity
        );

        final var petImageEntity2 = createPetImageEntity(
                petDto.images().get(1).imageUrl(),
                petEntity
        );

        petEntity.setImages(List.of(petImageEntity1, petImageEntity2));

        // Mock
        when(mapper.mapToEntity(petDto)).thenReturn(petEntity);
        when(shelterRepository.findById(petDto.shelterId())).thenReturn(Optional.of(shelterEntity));
        when(petRepository.save(petEntity)).thenReturn(petEntity);

        // When
        createPet.create(petDto);

        // Then
        ArgumentCaptor<PetEntity> petEntityCaptor = forClass(PetEntity.class);
        verify(petRepository, times(1)).save(petEntityCaptor.capture());

        PetEntity savedPetEntity = petEntityCaptor.getValue();
        assertThat(savedPetEntity.getId()).isEqualTo(petEntity.getId());
        assertThat(savedPetEntity.getName()).isEqualTo(petEntity.getName());
        assertThat(savedPetEntity.getShelter()).isEqualTo(petEntity.getShelter());
        assertThat(savedPetEntity.getSpecie()).isEqualTo(petEntity.getSpecie());
        assertThat(savedPetEntity.getGender()).isEqualTo(petEntity.getGender());
        assertThat(savedPetEntity.getBirthDate()).isEqualTo(petEntity.getBirthDate());
        assertThat(savedPetEntity.getLocation()).isEqualTo(petEntity.getLocation());
        assertThat(savedPetEntity.getAvatar()).isEqualTo(petEntity.getAvatar());
        assertThat(savedPetEntity.getWeight()).isEqualTo(petEntity.getWeight());
        assertThat(savedPetEntity.getDescription()).isEqualTo(petEntity.getDescription());
        assertThat(savedPetEntity.getImages()).isEqualTo(petEntity.getImages());
    }

    @Test
    @DisplayName("Should return success when "
           + "registering multiple pets to the same shelter")
    void save_multiple_pets_success() {

        // Given
        final var shelterDto = createShelterDto();
        final var shelterEntity = createShelterEntity(shelterDto);
        shelterEntity.setPets(new ArrayList<>());
        final var petDto1 = createPetDto(shelterEntity.getId());
        final var petDto2 = createPetDto(shelterEntity.getId());
        final var petEntity1 = createPetEntityWithoutImages(petDto1, shelterEntity);
        final var petEntity2 = createPetEntityWithoutImages(petDto2, shelterEntity);

        // Mock
        when(mapper.mapToEntity(petDto1)).thenReturn(petEntity1);
        when(mapper.mapToEntity(petDto2)).thenReturn(petEntity2);
        when(shelterRepository.findById(petDto1.shelterId()))
                .thenReturn(Optional.of(shelterEntity));
        when(petRepository.save(petEntity1)).thenReturn(petEntity1);
        when(petRepository.save(petEntity2)).thenReturn(petEntity2);

        // When
        createPet.create(petDto1);
        createPet.create(petDto2);

        // Then
        ArgumentCaptor<PetEntity> petEntityCaptor = forClass(PetEntity.class);
        verify(petRepository, times(2)).save(petEntityCaptor.capture());

        List<PetEntity> savedPetEntities = petEntityCaptor.getAllValues();
        assertThat(savedPetEntities).hasSize(2);

        PetEntity savedPetEntity1 = savedPetEntities.get(0);
        PetEntity savedPetEntity2 = savedPetEntities.get(1);

        assertThat(savedPetEntity1.getShelter()).isEqualTo(shelterEntity);
        assertThat(savedPetEntity2.getShelter()).isEqualTo(shelterEntity);
        assertThat(shelterEntity.getPets()).contains(savedPetEntity1, savedPetEntity2);
    }
}
