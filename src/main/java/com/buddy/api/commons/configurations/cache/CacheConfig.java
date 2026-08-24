package com.buddy.api.commons.configurations.cache;

import com.buddy.api.domains.terms.dtos.TermsVersionDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

    public RedisSerializer<TermsVersionDto> termsSerializer(final ObjectMapper objectMapper) {
        return new Jackson2JsonRedisSerializer<>(objectMapper, TermsVersionDto.class);
    }

    @Bean
    public RedisCacheManager cacheManager(final RedisConnectionFactory redisConnectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        RedisSerializer<TermsVersionDto> termsSerializer = termsSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .disableCachingNullValues()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer));

        RedisCacheConfiguration termsConfig = defaultConfig.serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(termsSerializer));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("emailVerificationToken", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put("emailVerificationRateLimit",
            defaultConfig.entryTtl(Duration.ofMinutes(1)));

        cacheConfigs.put("forgotPasswordToken", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put("forgotPasswordRateLimit",
            defaultConfig.entryTtl(Duration.ofMinutes(1)));

        cacheConfigs.put("terms", termsConfig.entryTtl(Duration.ofHours(24)));

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(
        final RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
