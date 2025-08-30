package com.buddy.api.commons.configurations.cache;

import com.buddy.api.commons.exceptions.NotFoundException;
import jakarta.annotation.PostConstruct;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenManager {
    private final CacheInitializer cacheInitializer;
    private Cache verificationTokenCache;

    @PostConstruct
    public void init() {
        this.verificationTokenCache = cacheInitializer.initializeVerificationTokenCache();
    }

    public String generateAndStoreToken(final String userEmail) {
        String token = UUID.randomUUID().toString();
        verificationTokenCache.put(token, userEmail);
        return token;
    }

    public String validateAndGetTokenEmail(final String token, final UUID accountId) {
        String email = verificationTokenCache.get(token, String.class);
        if (email == null) {
            log.warn("Invalid or expired token for account={}. Token: {}",
                accountId, token.substring(0, Math.min(token.length(), 8)) + "...");
            throw new NotFoundException("token", "Invalid or expired verification token.");
        }
        return email;
    }

    public void evictToken(final String token) {
        verificationTokenCache.evict(token);
    }
}
