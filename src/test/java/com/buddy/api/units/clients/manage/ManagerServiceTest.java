package com.buddy.api.units.clients.manage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.properties.ManagerApiProperties;
import com.buddy.api.integrations.clients.configs.executor.ApiClientExecutor;
import com.buddy.api.integrations.clients.manager.ManagerClient;
import com.buddy.api.integrations.clients.manager.ManagerService;
import com.buddy.api.integrations.clients.manager.request.ManagerAuthRequest;
import com.buddy.api.integrations.clients.manager.response.ManagerAuthResponse;
import com.buddy.api.units.UnitTestAbstract;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@DisplayName("Unit Tests for ManagerService")
class ManagerServiceTest extends UnitTestAbstract {

    private static final String CACHE_KEY = "manager-api:token";
    private static final String INTEGRATION_NAME = "Manager API - Authentication";

    @Mock
    private ManagerClient managerClient;

    @Mock
    private ManagerApiProperties managerApiProperties;

    @Mock
    private ApiClientExecutor apiClientExecutor;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ManagerService managerService;

    @BeforeEach
    void setup() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("Cache Logic Scenarios")
    class CacheLogicScenarios {

        @Test
        @DisplayName(
            "Given a token exists in cache, "
                + "when getting a token, "
                + "then it should return the cached token"
        )
        void givenTokenInCache_whenGetValidToken_thenReturnsCachedToken() {
            final var cachedToken = UUID.randomUUID().toString();
            givenTokenExistsInCache(cachedToken);

            final var resultToken = managerService.getValidToken();

            assertThat(resultToken).isEqualTo(cachedToken);
            thenAssertNoApiCallWasMade();
        }

        @Test
        @DisplayName(
            "Given the cache is empty,"
                + " when getting a token, "
                + "then it should authenticate and cache the new token"
        )
        void givenCacheIsEmpty_whenGetValidToken_thenCallsLoginAndCachesResult() {
            final var expectedUsername = UUID.randomUUID().toString();
            final var expectedPassword = UUID.randomUUID().toString();
            final var expectedUserAgent = UUID.randomUUID().toString();
            final var expectedIpAddress = UUID.randomUUID().toString();
            final var authResponse = buildValidAuthResponse();

            givenCacheIsEmpty();
            when(managerApiProperties.username()).thenReturn(expectedUsername);
            when(managerApiProperties.password()).thenReturn(expectedPassword);
            when(managerApiProperties.userAgent()).thenReturn(expectedUserAgent);
            when(managerApiProperties.ipAddress()).thenReturn(expectedIpAddress);

            when(managerClient.login(any(ManagerAuthRequest.class),
                eq(expectedUserAgent),
                eq(expectedIpAddress)))
                .thenReturn(authResponse);

            when(apiClientExecutor.execute(eq(INTEGRATION_NAME), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<ManagerAuthResponse> supplier = invocation.getArgument(1);
                    return supplier.get();
                });

            final var resultToken = managerService.getValidToken();

            assertThat(resultToken).isEqualTo(authResponse.token());
            thenAssertNewTokenWasCached(authResponse.token(), authResponse.expiresIn());

            final var requestCaptor = ArgumentCaptor.forClass(ManagerAuthRequest.class);
            verify(managerClient, times(1))
                .login(requestCaptor.capture(), eq(expectedUserAgent), eq(expectedIpAddress));

            ManagerAuthRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.username()).isEqualTo(expectedUsername);
            assertThat(capturedRequest.password()).isEqualTo(expectedPassword);
            assertThat(capturedRequest.encryptedPassword()).isFalse();
        }

        @Test
        @DisplayName(
            "Given a thread is authenticating,"
                + " when another thread requests token,"
                + " then it should use the value from the first thread"
        )
        void givenThreadIsAuthenticating_whenAnotherThreadRequestsToken_thenItUsesCachedValue() {
            final var newToken = UUID.randomUUID().toString();
            givenCacheIsPopulatedDuringLock(newToken);

            final var resultToken = managerService.getValidToken();

            assertThat(resultToken).isEqualTo(newToken);
            thenAssertNoApiCallWasMade();
        }
    }

    @Nested
    @DisplayName("API Failure Scenarios")
    class FailureScenarios {

        @Test
        @DisplayName(
            "Given authentication returns a null token, "
                + "when getting a token, "
                + "then it should return null"
        )
        void givenAuthReturnsNullToken_whenGetValidToken_thenReturnsNull() {
            final var responseWithNullToken =
                ManagerAuthResponse.builder().token(null).expiresIn(3600).build();
            setupAuthenticationFlow(responseWithNullToken);

            final var resultToken = managerService.getValidToken();

            assertThat(resultToken).isNull();
            thenAssertNothingWasCached();
        }

        @Test
        @DisplayName(
            "Given the API executor throws an exception, "
                + "when getting a token, "
                + "then it should propagate the exception"
        )
        void givenApiExecutorThrowsException_whenGetValidToken_thenPropagatesException() {
            final var expectedException = new RuntimeException("API is down");
            givenCacheIsEmpty();
            when(apiClientExecutor.execute(eq(INTEGRATION_NAME), any(Supplier.class)))
                .thenThrow(expectedException);

            assertThatThrownBy(() -> managerService.getValidToken())
                .isInstanceOf(RuntimeException.class)
                .isEqualTo(expectedException);
        }

        @Test
        @DisplayName(
            "Given auth returns an invalid expiration, "
                + "when getting token, "
                + "then it should return token but not cache it")
        void givenAuthReturnsInvalidExpiration_whenGetValidToken_thenReturnsTokenButDoesNotCache() {
            final var responseWithInvalidExp = ManagerAuthResponse.builder()
                .token(UUID.randomUUID().toString())
                .expiresIn(0)
                .build();

            setupAuthenticationFlow(responseWithInvalidExp);

            final var resultToken = managerService.getValidToken();

            assertThat(resultToken).isEqualTo(responseWithInvalidExp.token());
            thenAssertNothingWasCached();
        }
    }

    private void givenTokenExistsInCache(final String token) {
        when(valueOperations.get(CACHE_KEY)).thenReturn(token);
    }

    private void givenCacheIsEmpty() {
        when(valueOperations.get(CACHE_KEY)).thenReturn(null);
    }

    private void givenCacheIsPopulatedDuringLock(final String newToken) {
        when(valueOperations.get(CACHE_KEY))
            .thenReturn(null)
            .thenReturn(newToken);
    }

    private void setupAuthenticationFlow(final ManagerAuthResponse responseToReturn) {
        final var authRequest = buildValidAuthRequest();
        givenCacheIsEmpty();

        when(managerApiProperties.username()).thenReturn(authRequest.username());
        when(managerApiProperties.password()).thenReturn(authRequest.password());
        when(managerApiProperties.userAgent()).thenReturn(UUID.randomUUID().toString());
        when(managerApiProperties.ipAddress()).thenReturn(UUID.randomUUID().toString());

        when(managerClient.login(
            authRequest,
            managerApiProperties.userAgent(),
            managerApiProperties.ipAddress())
        ).thenReturn(responseToReturn);

        when(apiClientExecutor.execute(eq(INTEGRATION_NAME), any(Supplier.class)))
            .thenAnswer(invocation ->
                invocation.<Supplier<ManagerAuthResponse>>getArgument(1).get()
            );
    }

    private ManagerAuthResponse buildValidAuthResponse() {
        return ManagerAuthResponse.builder()
            .token(UUID.randomUUID().toString())
            .expiresIn(3600000)
            .build();
    }

    private ManagerAuthRequest buildValidAuthRequest() {
        return ManagerAuthRequest.builder()
            .username(UUID.randomUUID().toString())
            .password(UUID.randomUUID().toString())
            .encryptedPassword(false)
            .build();
    }

    private void thenAssertNoApiCallWasMade() {
        verify(apiClientExecutor, never()).execute(anyString(), any(Supplier.class));
    }

    private void thenAssertNothingWasCached() {
        verify(valueOperations, never())
            .set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    private void thenAssertNewTokenWasCached(final String expectedToken,
                                             final long expectedExpiration) {
        ArgumentCaptor<Long> expirationCaptor = ArgumentCaptor.forClass(Long.class);
        verify(valueOperations, times(1)).set(
            eq(CACHE_KEY),
            eq(expectedToken),
            expirationCaptor.capture(),
            eq(TimeUnit.MILLISECONDS)
        );
        assertThat(expirationCaptor.getValue()).isEqualTo(expectedExpiration);
    }
}