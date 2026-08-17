package com.buddy.api.units.commons.configurations.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.buddy.api.commons.configurations.cache.CacheConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Should successfully serialize and deserialize a string token")
    void should_serialize_and_deserialize_string_token() {
        ObjectMapper objectMapper = new ObjectMapper();
        RedisSerializer<Object> serializer = cacheConfig.jsonSerializer(objectMapper);
        String token = "my-secure-token-12345";
        
        byte[] serialized = serializer.serialize(token);
        
        assertThat(serialized).isNotNull();

        Object deserialized = serializer.deserialize(serialized);
        
        assertThat(deserialized).isEqualTo(token);
    }

    @Test
    @DisplayName("Should handle serialization exceptions correctly")
    void should_handle_serialization_exceptions() {
        ObjectMapper objectMapper = new ObjectMapper();
        RedisSerializer<Object> serializer = cacheConfig.jsonSerializer(objectMapper);
        Object unSerializable = new Object();
        
        assertThatThrownBy(() -> serializer.serialize(unSerializable))
            .isInstanceOf(SerializationException.class);

        byte[] empty = serializer.serialize(null);
        
        assertThat(empty).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Should handle deserialization exceptions correctly")
    void should_handle_deserialization_exceptions() {
        ObjectMapper objectMapper = new ObjectMapper();
        RedisSerializer<Object> serializer = cacheConfig.jsonSerializer(objectMapper);
        byte[] invalidJson = "invalid-json".getBytes();
        
        assertThatThrownBy(() -> serializer.deserialize(invalidJson))
            .isInstanceOf(SerializationException.class);

        assertThat(serializer.deserialize(null)).isNull();
        assertThat(serializer.deserialize(new byte[0])).isNull();
    }
}
