package com.buddy.api.units.commons.configurations.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.cache.RateLimitChecker;
import com.buddy.api.commons.configurations.properties.RateLimitProperties;
import com.buddy.api.commons.exceptions.TooManyRequestsException;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class RateLimitCheckerTest extends UnitTestAbstract {

    @Mock
    private ProxyManager<byte[]> proxyManager;

    @Mock
    private RemoteBucketBuilder<byte[]> bucketBuilder;

    @Mock
    private BucketProxy bucket;

    @Mock
    private RateLimitProperties rateLimitProperties;

    @InjectMocks
    private RateLimitChecker rateLimitChecker;

    private String email;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        email = RandomEmailUtils.generateValidEmail();
        accountId = UUID.randomUUID();


        lenient().when(proxyManager.builder()).thenReturn(bucketBuilder);
        lenient().when(bucketBuilder.build(any(byte[].class), any(Supplier.class)))
            .thenReturn(bucket);
    }

    @Test
    @DisplayName("Should build bucket configuration from rate limit properties")
    void should_build_bucket_configuration() {
        when(rateLimitProperties.maxAttempts()).thenReturn(7);
        when(rateLimitProperties.windowMinutes()).thenReturn(3);
        when(bucket.tryConsume(1)).thenReturn(true);

        rateLimitChecker.checkRateLimit(email, accountId);

        var configurationCaptor =
            org.mockito.ArgumentCaptor.forClass(Supplier.class);
        verify(bucketBuilder, atLeastOnce()).build(any(byte[].class),
            configurationCaptor.capture());
        BucketConfiguration configuration =
            (BucketConfiguration) configurationCaptor.getValue().get();

        assertThat(configuration.getBandwidths()).hasSize(1);
        assertThat(configuration.getBandwidths()[0].getCapacity()).isEqualTo(7);
    }

    @Nested
    @DisplayName("Tests for checkRateLimit method")
    class CheckRateLimitTests {

        @Test
        @DisplayName("Should allow request when bucket tryConsume is true")
        void should_allow_request() {
            lenient().when(bucket.tryConsume(1)).thenReturn(true);

            assertThatNoException().isThrownBy(
                () -> rateLimitChecker.checkRateLimit(email, accountId));
        }

        @Test
        @DisplayName("Should throw TooManyRequestsException when limit exceeded")
        void should_throw_too_many_requests_exception_when_limit_exceeded() {
            lenient().when(bucket.tryConsume(1)).thenReturn(false);

            assertThatThrownBy(() -> rateLimitChecker.checkRateLimit(email, accountId))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessage(
                    "Too many verification requests. Please wait a minute before trying again.");
        }
    }

    @Nested
    @DisplayName("Tests for checkPasswordRecoveryRateLimit method")
    class CheckPasswordRecoveryRateLimitTests {

        @Test
        @DisplayName("Should allow request when bucket tryConsume is true")
        void should_allow_request() {
            lenient().when(bucket.tryConsume(1)).thenReturn(true);

            assertThatNoException().isThrownBy(
                () -> rateLimitChecker.checkPasswordRecoveryRateLimit(email, accountId));
        }

        @Test
        @DisplayName("Should throw TooManyRequestsException when limit exceeded")
        void should_throw_too_many_requests_exception_when_limit_exceeded() {
            lenient().when(bucket.tryConsume(1)).thenReturn(false);

            assertThatThrownBy(
                () -> rateLimitChecker.checkPasswordRecoveryRateLimit(email, accountId))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessage(
                    "Too many password recovery requests. "
                        + "Please wait a minute before trying again.");
        }
    }
}
