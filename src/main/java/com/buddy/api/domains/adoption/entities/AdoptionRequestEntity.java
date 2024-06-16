package com.buddy.api.domains.adoption.entities;

import com.buddy.api.domains.adoption.enums.AdoptionStatus;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.shelter.entities.ShelterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ADOPTION_REQUEST")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdoptionRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "pet_id")
    private PetEntity pet;

    @ManyToOne
    @JoinColumn(name = "shelter_id")
    private ShelterEntity shelter;

    private String userName;
    private String userEmail;

    @Enumerated(EnumType.STRING)
    private AdoptionStatus status = AdoptionStatus.PENDING;

    @Column(name = "REQUEST_CREATE")
    private LocalDateTime requestCreateDate;

    @Column(name = "REQUEST_UPDATE")
    private LocalDateTime requestUpdateDate;

    @PrePersist
    public void onPrePersist() {
        this.requestCreateDate = LocalDateTime.now();
        this.requestUpdateDate = LocalDateTime.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        this.requestUpdateDate = LocalDateTime.now();
    }
}
