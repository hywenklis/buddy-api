package com.buddy.api.domains.shelter.entities;

import com.buddy.api.domains.pet.entities.PetEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "SHELTER")
@Getter
@NoArgsConstructor
public class ShelterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nameShelter;
    private String nameResponsible;
    private String cpfResponsible;
    private String address;
    private String phoneNumber;
    private String email;
    private String avatar;

    @Getter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PetEntity> pets = new ArrayList<>();

    @Column(name = "CREATE_DATE")
    private LocalDateTime createDate;

    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;

    @Builder
    public ShelterEntity(UUID id,
                         String nameShelter,
                         String nameResponsible,
                         String cpfResponsible,
                         String address,
                         String phoneNumber,
                         String email,
                         String avatar,
                         List<PetEntity> pets,
                         LocalDateTime createDate,
                         LocalDateTime updateDate
    ) {
        this.id = id;
        this.nameShelter = nameShelter;
        this.nameResponsible = nameResponsible;
        this.cpfResponsible = cpfResponsible;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.avatar = avatar;
        this.pets = pets != null ? new ArrayList<>(pets) : new ArrayList<>();
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    @PrePersist
    public void onPrePersist() {
        this.createDate = LocalDateTime.now();
        this.updateDate = LocalDateTime.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updateDate = LocalDateTime.now();
    }
}
