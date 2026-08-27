package com.buddy.api.integrations.web.shelter.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.domains.shelter.entities.ShelterMemberEntity;
import com.buddy.api.domains.shelter.repositories.ShelterMemberRepository;
import com.buddy.api.integrations.IntegrationTestAbstract;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("ShelterMemberEntity — persistence integration")
class ShelterMemberEntityTest extends IntegrationTestAbstract {

    @Autowired
    private ShelterMemberRepository shelterMemberRepository;

    @Nested
    @DisplayName("Given a valid ShelterMemberEntity")
    class GivenValidShelterMember {

        @Test
        @DisplayName("Should persist and retrieve a member with entry_date correctly")
        void should_persist_member_with_entry_date() {
            // Arrange
            final var memberProfile = profileRepository.save(
                profileComponent.validProfileEntity().build()
            );
            final var shelterProfile = profileRepository.save(
                profileComponent.validProfileEntity().build()
            );
            final var entryDate = LocalDateTime.now().withNano(0);

            final var entity = ShelterMemberEntity.builder()
                .memberProfile(memberProfile)
                .shelterProfile(shelterProfile)
                .isAdmin(false)
                .entryDate(entryDate)
                .build();

            // Act
            final var saved = shelterMemberRepository.save(entity);
            final var found = shelterMemberRepository.findById(saved.getMemberId());

            // Assert
            assertThat(found).isPresent();
            final var member = found.get();
            assertThat(member.getMemberId()).isNotNull();
            assertThat(member.getMemberProfile().getProfileId())
                .isEqualTo(memberProfile.getProfileId());
            assertThat(member.getShelterProfile().getProfileId())
                .isEqualTo(shelterProfile.getProfileId());
            assertThat(member.getIsAdmin()).isFalse();
            assertThat(member.getEntryDate()).isNotNull();
            assertThat(member.getDepartureDate()).isNull();
            assertThat(member.getCreationDate()).isNotNull();
            assertThat(member.getUpdatedDate()).isNotNull();
        }

        @Test
        @DisplayName("Should persist and retrieve a member without entry_date correctly")
        void should_persist_member_without_entry_date() {
            // Arrange
            final var memberProfile = profileRepository.save(
                profileComponent.validProfileEntity().build()
            );
            final var shelterProfile = profileRepository.save(
                profileComponent.validProfileEntity().build()
            );

            final var entity = ShelterMemberEntity.builder()
                .memberProfile(memberProfile)
                .shelterProfile(shelterProfile)
                .isAdmin(true)
                .entryDate(null)
                .build();

            // Act
            final var saved = shelterMemberRepository.save(entity);
            final var found = shelterMemberRepository.findById(saved.getMemberId());

            // Assert
            assertThat(found).isPresent();
            final var member = found.get();
            assertThat(member.getMemberId()).isNotNull();
            assertThat(member.getMemberProfile().getProfileId())
                .isEqualTo(memberProfile.getProfileId());
            assertThat(member.getShelterProfile().getProfileId())
                .isEqualTo(shelterProfile.getProfileId());
            assertThat(member.getIsAdmin()).isTrue();
            assertThat(member.getEntryDate()).isNull();
            assertThat(member.getDepartureDate()).isNull();
            assertThat(member.getCreationDate()).isNotNull();
            assertThat(member.getUpdatedDate()).isNotNull();
        }
    }
}
