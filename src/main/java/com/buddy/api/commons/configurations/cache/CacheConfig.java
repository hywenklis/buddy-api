package com.buddy.api.commons.configurations.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
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
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(final RedisConnectionFactory redisConnectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("com.buddy.api")
            .allowIfSubType("java.util.ArrayList")
            .allowIfSubType("java.util.UUID")
            .allowIfSubType("java.util.HashMap")
            .allowIfSubType("java.util.HashSet")
            .allowIfSubType("java.time")
            .build();

        StdTypeResolverBuilder typeResolverBuilder = new StdTypeResolverBuilder() {
            @Override
            public PolymorphicTypeValidator subTypeValidator(final MapperConfig<?> config) {
                return ptv;
            }
        };

        typeResolverBuilder.init(JsonTypeInfo.Id.CLASS, null);
        typeResolverBuilder.inclusion(JsonTypeInfo.As.PROPERTY);

        objectMapper.setDefaultTyping(typeResolverBuilder);

        RedisSerializer<Object> jsonSerializer = new RedisSerializer<Object>() {
            @Override
            public byte[] serialize(final Object t) throws SerializationException {
                if (t == null) {
                    return new byte[0];
                }
                try {
                    return objectMapper.writeValueAsBytes(t);
                } catch (Exception ex) {
                    throw new SerializationException("Could not write JSON: " + ex.getMessage(), ex);
                }
            }

            @Override
            public Object deserialize(final byte[] bytes) throws SerializationException {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                try {
                    return objectMapper.readValue(bytes, Object.class);
                } catch (Exception ex) {
                    throw new SerializationException("Could not read JSON: " + ex.getMessage(), ex);
                }
            }
        };

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .disableCachingNullValues()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("emailVerificationToken", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put("emailVerificationRateLimit",
            defaultConfig.entryTtl(Duration.ofMinutes(1)));

        cacheConfigs.put("forgotPasswordToken", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put("forgotPasswordRateLimit",
            defaultConfig.entryTtl(Duration.ofMinutes(1)));

        cacheConfigs.put("terms", defaultConfig.entryTtl(Duration.ofHours(24)));

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
