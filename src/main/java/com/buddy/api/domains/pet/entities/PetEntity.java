package com.buddy.api.domains.pet.entities;

import com.buddy.api.domains.shelter.entities.ShelterEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PET")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String specie;
    private String sex;
    private Integer age;
    private Double weight;
    private String description;
    private String avatar;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PetImageEntity> images = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "shelter_id")
    private ShelterEntity shelter;

    @Column(name = "CREATE_DATE")
    private LocalDateTime createDate;

    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;

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
