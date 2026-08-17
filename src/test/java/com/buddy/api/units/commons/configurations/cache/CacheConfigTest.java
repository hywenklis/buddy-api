package com.buddy.api.units.commons.configurations.cache;

import com.buddy.api.commons.configurations.cache.CacheConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

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

    @Test
    void testSerializationException() {
        ObjectMapper objectMapper = new ObjectMapper();
        RedisSerializer<Object> serializer = cacheConfig.jsonSerializer(objectMapper);

        Object unSerializable = new Object();
        Assertions.assertThrows(
            SerializationException.class, () -> serializer.serialize(unSerializable));

        byte[] empty = serializer.serialize(null);
        Assertions.assertNotNull(empty);
        Assertions.assertEquals(0, empty.length);
    }

    @Test
    void testDeserializationException() {
        ObjectMapper objectMapper = new ObjectMapper();
        RedisSerializer<Object> serializer = cacheConfig.jsonSerializer(objectMapper);

        byte[] invalidJson = "invalid-json".getBytes();
        Assertions.assertThrows(
            org.springframework.data.redis.serializer.SerializationException.class,
            () -> serializer.deserialize(invalidJson));

        Assertions.assertNull(serializer.deserialize(null));
        Assertions.assertNull(serializer.deserialize(new byte[0]));
    }
}
