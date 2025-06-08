package com.buddy.api.domains.account.services.impl;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailVerificationCacheService {

    public static final String TOKEN_CACHE = "emailVerificationToken";
    public static final String RATELIMIT_CACHE = "emailVerificationRateLimit";

    @CachePut(value = TOKEN_CACHE, key = "#token")
    public UUID storeToken(final String token, final UUID accountId) {
        log.debug("Saving verification token: token={} accountId={}", token, accountId);
        return accountId;
    }

    @Cacheable(value = TOKEN_CACHE, key = "#token")
    public UUID getAccountIdByToken(final String token) {
        log.debug("Verification token not found in cache: token={}", token);
        return null;
    }

    @CacheEvict(value = TOKEN_CACHE, key = "#token")
    public void invalidateToken(final String token) {
        log.debug("Invalidating verification token={}", token);
    }

    @Cacheable(value = RATELIMIT_CACHE, key = "#accountId")
    public Boolean isRateLimited(final UUID accountId) {
        return Boolean.FALSE;
    }

    @CachePut(value = RATELIMIT_CACHE, key = "#accountId")
    public Boolean setRateLimit(final UUID accountId) {
        log.debug("rate limit: accountId={}", accountId);
        return Boolean.TRUE;
    }
}
