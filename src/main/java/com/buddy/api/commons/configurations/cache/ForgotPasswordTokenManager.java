package com.buddy.api.commons.configurations.cache;

import jakarta.annotation.PostConstruct;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

@Component
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

    public String getEmailByToken(final String token) {
        Cache.ValueWrapper valueWrapper = forgotPasswordTokenCache.get(token);
        if (valueWrapper != null && valueWrapper.get() != null) {
            return (String) valueWrapper.get();
        }
        return null;
    }

    public void invalidateToken(final String token) {
        forgotPasswordTokenCache.evict(token);
    }
}
