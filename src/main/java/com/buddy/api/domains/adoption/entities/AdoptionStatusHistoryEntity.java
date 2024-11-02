package com.buddy.api.domains.adoption.entities;

import com.buddy.api.domains.adoption.enums.AdoptionStatus;
import com.buddy.api.domains.shelter.entities.ShelterMemberEntity;
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
@Table(name = "adoption_status_history")
public class AdoptionStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "status_id", nullable = false, unique = true)
    private UUID statusId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "adoption_request_v2_id",
            referencedColumnName = "request_id",
            nullable = false
    )
    @ToString.Exclude
    private AdoptionRequestV2Entity adoptionRequestV2;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id", nullable = false)
    @ToString.Exclude
    private ShelterMemberEntity shelterMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_name", nullable = false)
    private AdoptionStatus statusName;

    @Column(name = "status_date", nullable = false)
    private LocalDateTime statusDate;

    @Column(name = "creation_date", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime creationDate;

    @Column(name = "updated_date")
    @UpdateTimestamp
    private LocalDateTime updatedDate;
}

