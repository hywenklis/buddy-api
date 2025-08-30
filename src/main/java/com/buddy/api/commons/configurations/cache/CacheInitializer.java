package com.buddy.api.commons.configurations.cache;

import com.buddy.api.commons.exceptions.CacheInitializationException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheInitializer {
    private static final String VERIFICATION_TOKEN_CACHE_NAME = "emailVerificationToken";
    private static final String RATE_LIMIT_CACHE_NAME = "emailVerificationRateLimit";

    private final CacheManager cacheManager;

    public Cache initializeVerificationTokenCache() {
        Cache verificationTokenCache = cacheManager.getCache(VERIFICATION_TOKEN_CACHE_NAME);
        Cache rateLimitCache = cacheManager.getCache(RATE_LIMIT_CACHE_NAME);

        if (verificationTokenCache == null || rateLimitCache == null) {
            throw new CacheInitializationException(
                "cache",
                "Required caches not found: emailVerificationToken or emailVerificationRateLimit");
        }
        return verificationTokenCache;
    }
}
