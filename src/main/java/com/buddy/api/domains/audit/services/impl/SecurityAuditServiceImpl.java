package com.buddy.api.domains.audit.services.impl;

import com.buddy.api.domains.audit.entities.SecurityAuditEventEntity;
import com.buddy.api.domains.audit.enums.SecurityEventType;
import com.buddy.api.domains.audit.repositories.SecurityAuditEventRepository;
import com.buddy.api.domains.audit.services.SecurityAuditService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SecurityAuditServiceImpl implements SecurityAuditService {

    private final SecurityAuditEventRepository repository;

    @Override
    @Transactional
    public void logEvent(
        final UUID accountId,
        final SecurityEventType eventType,
        final String ipAddress,
        final String userAgent
    ) {
        log.debug("Logging security event: {} for account: {}", eventType, accountId);
        
        final var event = SecurityAuditEventEntity.builder()
            .accountId(accountId)
            .eventType(eventType)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();
            
        repository.save(event);
    }
}
