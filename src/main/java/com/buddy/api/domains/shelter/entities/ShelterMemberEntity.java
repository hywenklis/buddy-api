package com.buddy.api.domains.shelter.entities;

import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.shelter.enums.MemberTypeEnum;
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
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "shelter_member")
public class ShelterMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "member_id", nullable = false, unique = true)
    private UUID memberId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "profile_id",
        referencedColumnName = "profile_id",
        nullable = false,
        updatable = false
    )
    @ToString.Exclude
    private ProfileEntity profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shelter_id",
        referencedColumnName = "shelter_id",
        nullable = false,
        updatable = false
    )
    private ShelterV2Entity shelter;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false)
    private MemberTypeEnum memberType;

    @Column(name = "is_owner", nullable = false)
    private Boolean isOwner;
}

