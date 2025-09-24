package com.buddy.api.integrations.clients.manager;

import com.buddy.api.commons.configurations.properties.ManagerApiProperties;
import com.buddy.api.integrations.clients.configs.executor.ApiClientExecutor;
import com.buddy.api.integrations.clients.manager.request.ManagerAuthRequest;
import com.buddy.api.integrations.clients.manager.request.ManagerEmailContent;
import com.buddy.api.integrations.clients.manager.request.ManagerGatewayRequest;
import com.buddy.api.integrations.clients.manager.response.ManagerAuthResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerService {
    private static final String MANAGER_API_TOKEN_CACHE_KEY = "manager-api:token";
    private static final String INTEGRATION_NAME = "manager-api";

    private final ManagerClient managerClient;
    private final ManagerApiProperties managerApiProperties;
    private final ApiClientExecutor apiClientExecutor;
    private final RedisTemplate<String, String> redisTemplate;
    private final Object tokenLock = new Object();

    public String getValidToken() {
        final Optional<String> cachedToken = findTokenInCache();
        if (cachedToken.isPresent()) {
            log.debug("Using cached Manager API token from Redis.");
            return cachedToken.get();
        }

        synchronized (tokenLock) {
            return findTokenInCache().orElseGet(this::authenticateAndCacheToken);
        }
    }

    @Async
    public CompletableFuture<Void> sendEmailNotification(final List<String> recipients,
                                                         final String from,
                                                         final String subject,
                                                         final String body
    ) {
        log.info("Preparing to send email notification to Manager API - Gateway");
        final String token = getValidToken();

        final var emailContent = new ManagerEmailContent(
            recipients,
            from,
            subject,
            body,
            true,
            List.of(),
            "buddy"
        );

        final var gatewayRequest = new ManagerGatewayRequest<>(
            managerApiProperties.appId(),
            "/api/v1/emails",
            "POST",
            emailContent
        );

        apiClientExecutor.execute(INTEGRATION_NAME, () ->
            managerClient.sendNotification(
                "Bearer " + token,
                gatewayRequest
            )
        );

        log.info("Email dispatch instruction sent successfuly to recipients: {}", recipients);
        return CompletableFuture.completedFuture(null);
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

        ManagerAuthResponse response = apiClientExecutor.execute(INTEGRATION_NAME, () ->
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