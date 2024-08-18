package com.buddy.api.domains.adoption.entities;

import com.buddy.api.domains.adoption.enums.AdoptionStatus;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.shelter.entities.ShelterEntity;
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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private PetEntity pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelter_id", nullable = false)
    private ShelterEntity shelter;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdoptionStatus status;

    @Column(name = "request_create_date", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime requestCreateDate;

    @Column(name = "request_update_date")
    @UpdateTimestamp
    private LocalDateTime requestUpdateDate;
}
