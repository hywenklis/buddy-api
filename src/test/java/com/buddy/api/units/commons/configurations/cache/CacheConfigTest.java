package com.buddy.api.units.commons.configurations.cache;

import com.buddy.api.commons.configurations.cache.CacheConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;

class CacheConfigTest {

    private CacheConfig cacheConfig;

    @BeforeEach
    void setUp() {
        cacheConfig = new CacheConfig();
    }

    @Test
    void testStringRoundTripSerialization() {
        ObjectMapper objectMapper = new ObjectMapper();

        RedisSerializer<Object> serializer = cacheConfig.jsonSerializer(objectMapper);

        String token = "my-secure-token-12345";
        byte[] serialized = serializer.serialize(token);

        Assertions.assertNotNull(serialized);

        Object deserialized = serializer.deserialize(serialized);
        Assertions.assertEquals(token, deserialized);
    }
}
