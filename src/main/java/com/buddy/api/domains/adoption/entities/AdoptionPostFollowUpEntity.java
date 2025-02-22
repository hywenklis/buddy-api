package com.buddy.api.domains.adoption.entities;

import com.buddy.api.domains.adoption.enums.AdaptationStatusEnum;
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
@Table(name = "adoption_post_follow_up")
public class AdoptionPostFollowUpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "follow_up_id", nullable = false, unique = true)
    private UUID followUpId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "adoption_request_v2_id",
            referencedColumnName = "adoption_request_v2_id",
            nullable = false,
            updatable = false
    )
    @ToString.Exclude
    private AdoptionRequestV2Entity adoptionRequestV2;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "profile_id",
            referencedColumnName = "profile_id",
            nullable = false,
            updatable = false
    )
    @ToString.Exclude
    private ProfileEntity followUpCreatorProfile;

    @Column(name = "visit_date")
    private LocalDateTime visitDate;

    @Column(name = "report")
    private String report;

    @Enumerated(EnumType.STRING)
    @Column(name = "adaptation_status", nullable = false)
    private AdaptationStatusEnum adaptationStatus;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "creation_date", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime creationDate;

    @Column(name = "updated_date", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedDate;
}
