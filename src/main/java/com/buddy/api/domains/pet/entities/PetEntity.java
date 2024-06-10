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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PET")
@Getter
@Setter
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

    @Builder
    public PetEntity(UUID id,
                     String name,
                     String specie,
                     String sex,
                     Integer age,
                     Double weight,
                     String description,
                     String avatar,
                     List<PetImageEntity> images,
                     LocalDateTime createDate,
                     LocalDateTime updateDate,
                     ShelterEntity shelter) {
        this.id = id;
        this.name = name;
        this.specie = specie;
        this.sex = sex;
        this.age = age;
        this.weight = weight;
        this.description = description;
        this.avatar = avatar;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.shelter = shelter;
        if (images != null) {
            this.images = new ArrayList<>(images);
            this.images.forEach(image -> image.setPet(this));
        }
    }

    public List<PetImageEntity> getImages() {
        return Collections.unmodifiableList(images);
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
