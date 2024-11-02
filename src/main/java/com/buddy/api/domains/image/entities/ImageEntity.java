package com.buddy.api.domains.image.entities;

import com.buddy.api.domains.image.enums.ImageStatus;
import com.buddy.api.domains.pet.entities.PetV2Entity;
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
import jakarta.persistence.Lob;
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
@Table(name = "image")
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "image_id", nullable = false, unique = true)
    private UUID imageId;

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
    @JoinColumn(
        name = "pet_v2_id",
        referencedColumnName = "pet_v2_id",
        nullable = false,
        updatable = false
    )
    @ToString.Exclude
    private PetV2Entity pet;

    @Column(name = "is_avatar", nullable = false)
    private Boolean isAvatar;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Lob
    @Column(name = "image_data")
    private byte[] imageData;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_status")
    private ImageStatus imageStatus;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "creation_date", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime creationDate;

    @Column(name = "updated_date", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedDate;
}

