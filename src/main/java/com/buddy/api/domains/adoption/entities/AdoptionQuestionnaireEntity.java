package com.buddy.api.domains.adoption.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "adoption_questionnaire")
public class AdoptionQuestionnaireEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "adoption_questionnaire_id", nullable = false, unique = true)
    private UUID adoptionQuestionnaireId;

    @Column(name = "housing_type")
    private String housingType;

    @Column(name = "has_other_pets", nullable = false)
    private Boolean hasOtherPets;

    @Column(name = "family_routine")
    private String familyRoutine;

    @Column(name = "previous_experience")
    private String previousExperience;

    @Column(name = "motivation")
    private String motivation;

    @Column(name = "home_visit_agreement", nullable = false)
    private Boolean homeVisitAgreement;

    @Column(name = "follow_up_agreement", nullable = false)
    private Boolean followUpAgreement;

    @Column(name = "creation_date", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime creationDate;

    @Column(name = "updated_date", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedDate;
}

