package com.buddy.api.domains.shelter.entities;

import com.buddy.api.domains.pet.entities.PetEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "SHELTER")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShelterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name_shelter", nullable = false)
    private String nameShelter;

    @Column(name = "name_responsible", nullable = false)
    private String nameResponsible;

    @Column(name = "cpf_responsible", nullable = false, unique = true)
    private String cpfResponsible;

    private String address;
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String email;

    private String avatar;

    @OneToMany(mappedBy = "shelter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PetEntity> pets = new ArrayList<>();

    @Column(name = "create_date", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createDate;

    @Column(name = "update_date")
    @UpdateTimestamp
    private LocalDateTime updateDate;
}
