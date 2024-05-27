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
import java.util.Collections;
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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PetEntity> pets;

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
        this.pets = pets == null ? List.of() : List.copyOf(pets);
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    public List<PetEntity> getPets() {
        return Collections.unmodifiableList(pets);
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

    public static class ShelterEntityBuilder {
        private List<PetEntity> pets;

        public void pets(List<PetEntity> pets) {
            this.pets = pets == null ? List.of() : List.copyOf(pets);
        }
    }
}
