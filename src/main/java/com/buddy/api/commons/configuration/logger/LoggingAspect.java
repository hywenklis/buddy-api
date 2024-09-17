package com.buddy.api.commons.configuration.logger;

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

    @Before("execution(* com.buddy.api..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String parameters = Arrays.stream(joinPoint.getArgs())
                .map(this::maskSensitiveData)
                .collect(Collectors.joining(", "));

        logger.info("Class: {}, Method: {}, Parameters: [{}]", className, methodName, parameters);
    }

    @AfterReturning(pointcut = "execution(* com.buddy.api..*(..))", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String maskedResult = maskSensitiveData(result);

        logger.info("Class: {}, Method: {}, Result: [{}]", className, methodName, maskedResult);
    }

    private String maskSensitiveData(Object obj) {
        if (obj == null) {
            return "null";
        }
        String str = obj.toString();

        if (str.matches("\\d{11}")) {
            return str.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.***.***-$4");
        } else if (str.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")) {
            return str.replaceAll("(\\d{3})\\.\\d{3}\\.\\d{3}-(\\d{2})", "$1.***.***-$2");
        }

        if (str.matches(".*@.*")) {
            return str.replaceAll("(^[^@]+)(@.*)", "****$2");
        }

        return str;
    }
}
