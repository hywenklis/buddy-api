package com.buddy.api.units.commons.configurations.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.cache.annotations.RateLimited;
import com.buddy.api.commons.configurations.cache.aspects.RateLimitAspect;
import com.buddy.api.commons.configurations.properties.RateLimitProperties;
import com.buddy.api.commons.exceptions.TooManyRequestsException;
import com.buddy.api.units.UnitTestAbstract;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class RateLimitAspectTest extends UnitTestAbstract {

    @Mock
    private ProxyManager<byte[]> proxyManager;

    @Mock
    private RemoteBucketBuilder<byte[]> bucketBuilder;

    @Mock
    private BucketProxy bucket;

    @Mock
    private RateLimitProperties rateLimitProperties;

    @InjectMocks
    private RateLimitAspect aspect;

    @Test
    @DisplayName("Should throw when annotated operation exceeds its limit")
    void should_throw_when_limit_exceeded() throws Exception {
        when(proxyManager.builder()).thenReturn(bucketBuilder);
        when(bucketBuilder.build(any(byte[].class), any(Supplier.class))).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(false);

        JoinPoint joinPoint = joinPoint("test@example.com");
        RateLimited annotation = method().getAnnotation(RateLimited.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertThatThrownBy(() -> aspect.checkRateLimit(joinPoint, annotation))
            .isInstanceOf(TooManyRequestsException.class);
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Should accept requests with an invalid email expression")
    void should_accept_invalid_email_expression() throws Exception {
        when(proxyManager.builder()).thenReturn(bucketBuilder);
        when(bucketBuilder.build(any(byte[].class), any(Supplier.class))).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);

        RateLimited annotation = methodWithInvalidExpression().getAnnotation(RateLimited.class);
        aspect.checkRateLimit(joinPoint("email@example.com"), annotation);
    }

    @Test
    @DisplayName("Should create bucket configuration from rate limit properties")
    void should_create_bucket_configuration() throws Exception {
        when(proxyManager.builder()).thenReturn(bucketBuilder);
        when(bucketBuilder.build(any(byte[].class), any(Supplier.class))).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(rateLimitProperties.maxAttempts()).thenReturn(4);
        when(rateLimitProperties.windowMinutes()).thenReturn(2);

        aspect.checkRateLimit(joinPoint("email@example.com"),
            method().getAnnotation(RateLimited.class));
        var captor = org.mockito.ArgumentCaptor.forClass(Supplier.class);
        org.mockito.Mockito.verify(bucketBuilder).build(any(byte[].class), captor.capture());
        assertThat((io.github.bucket4j.BucketConfiguration) captor.getValue().get()).isNotNull();
    }

    private JoinPoint joinPoint(final String email) throws Exception {
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getParameterNames()).thenReturn(new String[] {"email"});
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[] {email});
        return joinPoint;
    }

    private Method method() throws Exception {
        return Fixture.class.getDeclaredMethod("limited", String.class);
    }

    private Method methodWithInvalidExpression() throws Exception {
        return Fixture.class.getDeclaredMethod("invalid", String.class);
    }

    static class Fixture {
        @RateLimited(operation = "test", emailSpel = "#email", useIp = true)
        void limited(final String email) {
        }

        @RateLimited(operation = "test", emailSpel = "#[", useIp = false)
        void invalid(final String email) {
        }
    }
}
