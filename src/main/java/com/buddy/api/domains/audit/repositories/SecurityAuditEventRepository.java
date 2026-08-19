package com.buddy.api.domains.audit.repositories;

import com.buddy.api.domains.audit.entities.SecurityAuditEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityAuditEventRepository
    extends JpaRepository<SecurityAuditEventEntity, UUID> {
}
