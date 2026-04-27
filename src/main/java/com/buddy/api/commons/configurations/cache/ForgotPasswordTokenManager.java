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
public class ForgotPasswordTokenManager {
    private final CacheInitializer cacheInitializer;
    private Cache forgotPasswordTokenCache;

    @PostConstruct
    public void init() {
        this.forgotPasswordTokenCache = cacheInitializer.initializeForgotPasswordTokenCache();
    }

    public String generateAndStoreToken(final String userEmail) {
        String token = UUID.randomUUID().toString();
        forgotPasswordTokenCache.put(token, userEmail);
        return token;
    }

    public String validateAndGetTokenEmail(final String token, final UUID accountId) {
        String email = forgotPasswordTokenCache.get(token, String.class);
        if (email == null) {
            log.warn("Invalid or expired forgot-password token for account={}. Token: {}",
                accountId, token.substring(0, Math.min(token.length(), 8)) + "...");
            throw new NotFoundException("token", "Invalid or expired forgot-password token.");
        }
        return email;
    }

    public void evictToken(final String token) {
        forgotPasswordTokenCache.evict(token);
    }
}
