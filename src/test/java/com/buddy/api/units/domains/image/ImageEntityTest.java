package com.buddy.api.domains.image.entities;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.buddy.api.commons.exceptions.DomainException;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.units.UnitTestAbstract;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ImageEntity — XOR constraint validation")
class ImageEntityTest extends UnitTestAbstract {

    private static final String XOR_MESSAGE =
        "'image' must be linked to exactly one of 'profile' or 'petV2', not both or neither.";

    private ProfileEntity buildProfile() {
        return ProfileEntity.builder()
            .profileId(UUID.randomUUID())
            .name("Test Profile")
            .profileType(ProfileTypeEnum.USER)
            .isDeleted(false)
            .build();
    }

    private PetV2Entity buildPetV2() {
        return PetV2Entity.builder()
            .petV2Id(UUID.randomUUID())
            .name("Test Pet")
            .species(PetSpecies.DOG)
            .gender(PetGender.MALE)
            .isForAdoption(false)
            .build();
    }

    @Nested
    @DisplayName("When XOR constraint is violated")
    class WhenConstraintViolated {

        @Test
        @DisplayName("Should throw DomainException when both profile and petV2 are null")
        void should_throw_domain_exception_when_both_are_null() {
            // Arrange
            final var entity = ImageEntity.builder()
                .profile(null)
                .petV2(null)
                .isAvatar(false)
                .filePath("path/to/image.jpg")
                .displayOrder(1)
                .build();

            // Act & Assert
            assertThatThrownBy(entity::validateXorConstraint)
                .isInstanceOf(DomainException.class)
                .hasMessage(XOR_MESSAGE)
                .extracting("fieldName")
                .isEqualTo("image");
        }

        @Test
        @DisplayName("Should throw DomainException when both profile and petV2 are non-null")
        void should_throw_domain_exception_when_both_are_non_null() {
            // Arrange
            final var entity = ImageEntity.builder()
                .profile(buildProfile())
                .petV2(buildPetV2())
                .isAvatar(false)
                .filePath("path/to/image.jpg")
                .displayOrder(1)
                .build();

            // Act & Assert
            assertThatThrownBy(entity::validateXorConstraint)
                .isInstanceOf(DomainException.class)
                .hasMessage(XOR_MESSAGE)
                .extracting("fieldName")
                .isEqualTo("image");
        }
    }

    @Nested
    @DisplayName("When XOR constraint is satisfied")
    class WhenConstraintSatisfied {

        @Test
        @DisplayName("Should not throw exception when only profile is set")
        void should_not_throw_when_only_profile_is_set() {
            // Arrange
            final var entity = ImageEntity.builder()
                .profile(buildProfile())
                .petV2(null)
                .isAvatar(true)
                .filePath("path/to/profile-image.jpg")
                .displayOrder(1)
                .build();

            // Act & Assert
            assertThatCode(entity::validateXorConstraint)
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should not throw exception when only petV2 is set")
        void should_not_throw_when_only_pet_v2_is_set() {
            // Arrange
            final var entity = ImageEntity.builder()
                .profile(null)
                .petV2(buildPetV2())
                .isAvatar(false)
                .filePath("path/to/pet-image.jpg")
                .displayOrder(2)
                .build();

            // Act & Assert
            assertThatCode(entity::validateXorConstraint)
                .doesNotThrowAnyException();
        }
    }
}
