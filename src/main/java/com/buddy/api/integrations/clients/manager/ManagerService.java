package com.buddy.api.integrations.clients.manager;

import com.buddy.api.commons.configurations.properties.ManagerApiProperties;
import com.buddy.api.integrations.clients.configs.executor.ApiClientExecutor;
import com.buddy.api.integrations.clients.manager.request.ManagerAuthRequest;
import com.buddy.api.integrations.clients.manager.response.ManagerAuthResponse;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerService {
    private static final String MANAGER_API_TOKEN_CACHE_KEY = "manager-api:token";

    private final ManagerClient managerClient;
    private final ManagerApiProperties managerApiProperties;
    private final ApiClientExecutor apiClientExecutor;
    private final RedisTemplate<String, String> redisTemplate;

    public String getValidToken() {
        Optional<String> cachedToken = findTokenInCache();
        if (cachedToken.isPresent()) {
            log.debug("Using cached Manager API token from Redis.");
            return cachedToken.get();
        }

        synchronized (this) {
            return findTokenInCache().orElseGet(this::authenticateAndCacheToken);
        }
    }

    private Optional<String> findTokenInCache() {
        return Optional.ofNullable(redisTemplate.opsForValue().get(MANAGER_API_TOKEN_CACHE_KEY));
    }

    private String authenticateAndCacheToken() {
        log.info("Manager API token not in cache or expired. Fetching a new one.");

        final var request = new ManagerAuthRequest(
            managerApiProperties.username(),
            managerApiProperties.password(),
            false
        );

        final String integrationName = "Manager API Authentication";

        ManagerAuthResponse response = apiClientExecutor.executeClientCall(integrationName, () ->
            managerClient.login(
                request,
                managerApiProperties.userAgent(),
                managerApiProperties.ipAddress()
            )
        );

        String newToken = response.token();
        long expiresInMillis = response.expiresIn();

        if (newToken != null && expiresInMillis > 0) {
            redisTemplate.opsForValue().set(
                MANAGER_API_TOKEN_CACHE_KEY,
                newToken,
                expiresInMillis,
                TimeUnit.MILLISECONDS
            );
            log.info(
                "Successfully authenticated with Manager API and cached the new token in Redis."
            );
        }
        return newToken;
    }
}