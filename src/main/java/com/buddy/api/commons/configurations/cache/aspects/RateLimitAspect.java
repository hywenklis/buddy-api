package com.buddy.api.commons.configurations.cache.aspects;

import com.buddy.api.commons.configurations.cache.annotations.RateLimited;
import com.buddy.api.commons.configurations.properties.RateLimitProperties;
import com.buddy.api.commons.exceptions.TooManyRequestsException;
import com.buddy.api.commons.http.ClientIpResolver;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    private static final String RATE_LIMIT_COUNT_KEY_PREFIX = "rate-limit:count:";
    private final ProxyManager<byte[]> proxyManager;
    private final RateLimitProperties rateLimitProperties;
    private final SpelExpressionParser spelParser = new SpelExpressionParser();
    @Value("${buddy.security.trusted-proxy-addresses:}")
    private List<String> trustedProxyAddresses = List.of();

    @Before("@annotation(rateLimited)")
    public void checkRateLimit(final JoinPoint joinPoint, final RateLimited rateLimited) {
        String email = extractEmail(joinPoint, rateLimited.emailSpel());
        String ip = rateLimited.useIp() ? extractClientIp() : "";
        boolean emailAllowed = consume(rateLimited.operation(), "email", email);
        boolean ipAllowed = !rateLimited.useIp()
            || consume(rateLimited.operation(), "ip", ip);

        if (!emailAllowed || !ipAllowed) {
            log.warn("Rate limit exceeded for {} request. email={}, ip={}", rateLimited.operation(),
                email, ip);
            throw new TooManyRequestsException(rateLimited.limitMessage());
        }
    }

    private boolean consume(final String operation, final String dimension, final String value) {
        String key = String.join(":", RATE_LIMIT_COUNT_KEY_PREFIX + operation, dimension, value);
        Bucket bucket = proxyManager.builder()
            .build(key.getBytes(StandardCharsets.UTF_8), this::bucketConfiguration);
        return bucket.tryConsume(1);
    }

    private String extractEmail(final JoinPoint joinPoint, final String emailSpel) {
        if (StringUtils.isBlank(emailSpel)) {
            return "";
        }
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            EvaluationContext context = new StandardEvaluationContext();

            for (int i = 0; i < signature.getParameterNames().length; i++) {
                context.setVariable(signature.getParameterNames()[i], joinPoint.getArgs()[i]);
            }

            return Optional.ofNullable(spelParser.parseExpression(emailSpel).getValue(context))
                .map(Object::toString)
                .orElse("");
        } catch (org.springframework.expression.ExpressionException e) {
            log.error("Failed to evaluate SpEL expression [{}]", emailSpel, e);
            return "";
        }
    }

    private String extractClientIp() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
            .map(ServletRequestAttributes.class::cast)
            .map(ServletRequestAttributes::getRequest)
            .map(request -> ClientIpResolver.extract(request, trustedProxyAddresses))
            .orElse("0.0.0.0");
    }

    private BucketConfiguration bucketConfiguration() {
        return BucketConfiguration.builder()
            .addLimit(Bandwidth.builder()
                .capacity(rateLimitProperties.maxAttempts())
                .refillIntervally(rateLimitProperties.maxAttempts(),
                    Duration.ofMinutes(rateLimitProperties.windowMinutes()))
                .build()
            )
            .build();
    }
}
