package com.buddy.api.domains.audit.services;

import com.buddy.api.domains.audit.enums.SecurityEventType;
import java.util.UUID;

public interface SecurityAuditService {
    void logEvent(UUID accountId, SecurityEventType eventType, String ipAddress, String userAgent);
}
