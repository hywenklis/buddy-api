package com.buddy.api.domains.account.entities;

import com.buddy.api.domains.valueobjects.EmailAddress;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "account")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "account_id", nullable = false, unique = true)
    private UUID accountId;

    @Column
    @Embedded
    private EmailAddress email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "terms_of_user_consent", nullable = false)
    private Boolean termsOfUserConsent;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;

    @Column(name = "is_blocked", nullable = false)
    private Boolean isBlocked;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "creation_date", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime creationDate;

    @Column(name = "updated_date", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedDate;
}
