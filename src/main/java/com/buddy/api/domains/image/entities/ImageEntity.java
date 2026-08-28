package com.buddy.api.domains.image.entities;

import com.buddy.api.commons.exceptions.DomainException;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.http.HttpStatus;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "profile_id",
        referencedColumnName = "profile_id",
        updatable = false
    )
    @ToString.Exclude
    private ProfileEntity profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "pet_v2_id",
        referencedColumnName = "pet_v2_id",
        updatable = false
    )
    @ToString.Exclude
    private PetV2Entity petV2;

    @Column(name = "is_avatar", nullable = false)
    private Boolean isAvatar;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "image_data")
    private byte[] imageData;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_status")
    private ImageStatus imageStatus;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "creation_date", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime creationDate;

    @Column(name = "updated_date", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedDate;

    @PrePersist
    protected void validateXorConstraint() {
        if ((profile == null) == (petV2 == null)) {
            throw new DomainException(
                "'image' must be linked to exactly one of 'profile' or 'petV2', "
                    + "not both or neither.",
                "image",
                HttpStatus.UNPROCESSABLE_ENTITY,
                null
            );
        }
    }
}
