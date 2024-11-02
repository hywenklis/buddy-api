package com.buddy.api.domains.adoption.entities;

import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.shelter.entities.ShelterV2Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "adoption_request_v2")
public class AdoptionRequestV2Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "adoption_request_v2_id", nullable = false, unique = true)
    private UUID adoptionRequestV2Id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "pet_id",
            referencedColumnName = "pet_id",
            nullable = false,
            updatable = false
    )
    @ToString.Exclude
    private PetV2Entity pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "adopter_profile_id",
            referencedColumnName = "profile_id",
            nullable = false,
            updatable = false
    )
    @ToString.Exclude
    private ProfileEntity adopterProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "pet_owner_profile_id",
            referencedColumnName = "profile_id",
            nullable = false,
            updatable = false
    )
    @ToString.Exclude
    private ProfileEntity petOwnerProfile;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "questionnaire_id",
            referencedColumnName = "questionnaire_id",
            nullable = false,
            updatable = false
    )
    @ToString.Exclude
    private AdoptionQuestionnaireEntity questionnaire;

    @Column(name = "creation_date", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime creationDate;

    @Column(name = "updated_date", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedDate;
}

