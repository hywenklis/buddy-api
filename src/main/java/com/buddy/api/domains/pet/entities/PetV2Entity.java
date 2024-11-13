package com.buddy.api.domains.pet.entities;

import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "pet_v2")
public class PetV2Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "pet_v2_id", nullable = false, unique = true)
    private UUID petV2Id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "profile_id",
        referencedColumnName = "profile_id",
        nullable = false
    )
    @ToString.Exclude
    private ProfileEntity guardianProfile;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "species", nullable = false)
    private PetSpecies species;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private PetGender gender;

    @Column(name = "approximate_age")
    private Integer approximateAge;

    @Column(name = "age_report_date")
    private LocalDate ageReportDate;

    @Column(name = "size", precision = 10, scale = 2)
    private BigDecimal size;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_neutered")
    private Boolean isNeutered;

    @Column(name = "is_for_adoption", nullable = false)
    private Boolean isForAdoption;

    @Column(name = "creation_date", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime creationDate;

    @Column(name = "updated_date", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedDate;
}

