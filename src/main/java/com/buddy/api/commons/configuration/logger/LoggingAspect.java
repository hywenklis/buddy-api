package com.buddy.api.commons.configuration.logger;

import com.buddy.api.commons.configuration.crypto.SensitiveDataMasker;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private final SensitiveDataMasker sensitiveDataMasker = new SensitiveDataMasker();

    @Before("execution(* com.buddy.api..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        final String className = joinPoint.getTarget().getClass().getSimpleName();
        final String methodName = joinPoint.getSignature().getName();
        final String parameters = formatParameters(joinPoint.getArgs());

        logger.info("Class: {}, Method: {}, Parameters: [{}]", className, methodName, parameters);
    }

    @AfterReturning(pointcut = "execution(* com.buddy.api..*(..))", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        final String className = joinPoint.getTarget().getClass().getSimpleName();
        final String methodName = joinPoint.getSignature().getName();
        final String maskedResult = sensitiveDataMasker.mask(result);

        logger.info("Class: {}, Method: {}, Result: [{}]", className, methodName, maskedResult);
    }

    private String formatParameters(Object[] args) {
        return Arrays.stream(args)
                .map(sensitiveDataMasker::mask)
                .collect(Collectors.joining(", "));
    }
}
