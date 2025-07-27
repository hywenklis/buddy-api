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

    // A chave de cache é única e global, pois o token é para o sistema, não por usuário.
    private static final String MANAGER_API_TOKEN_CACHE_KEY = "manager-api:token";

    private final ManagerClient managerClient;
    private final ManagerApiProperties managerApiProperties;
    private final ApiClientExecutor apiClientExecutor;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Obtém um token de autenticação de sistema válido, utilizando o padrão
     * "double-checked locking" para garantir performance e segurança em ambiente concorrente.
     *
     * @return Um token de autenticação válido.
     */
    public String getValidToken() {
        // 1. Primeira verificação (otimização): Tenta obter o token sem bloqueio.
        // Na maioria das vezes, o token estará aqui, evitando o custo do 'synchronized'.
        Optional<String> cachedToken = findTokenInCache();
        if (cachedToken.isPresent()) {
            log.debug("Using cached Manager API token from Redis.");
            return cachedToken.get();
        }

        // 2. Bloco sincronizado: Se o token não foi encontrado, garante que apenas uma
        // thread por vez tentará obter um novo token, evitando uma "estampida" de requisições.
        synchronized (this) {
            // 3. Segunda verificação (essencial para eficiência):
            // A thread verifica o cache novamente. Outra thread pode ter populado o cache
            // enquanto esta esperava para entrar no bloco.
            // O orElseGet garante que a chamada para buscar um novo token só ocorra
            // se o cache ainda estiver vazio.
            return findTokenInCache().orElseGet(this::authenticateAndCacheToken);
        }
    }

    /**
     * Busca o token no cache Redis de forma encapsulada.
     *
     * @return um Optional contendo o token se ele existir, ou um Optional vazio caso contrário.
     */
    private Optional<String> findTokenInCache() {
        return Optional.ofNullable(redisTemplate.opsForValue().get(MANAGER_API_TOKEN_CACHE_KEY));
    }

    /**
     * Autentica-se na Manager API, busca um novo token e o armazena no cache Redis
     * com o tempo de expiração (TTL) dinâmico fornecido pela API.
     *
     * @return O novo token de autenticação.
     */
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
            log.info("Successfully authenticated with Manager API and cached the new token in Redis.");
        }
        return newToken;
    }
}