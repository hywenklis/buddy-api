package com.buddy.api.units.commons.configurations.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.buddy.api.builders.terms.TermsBuilder;
import com.buddy.api.commons.configurations.cache.CacheConfig;
import com.buddy.api.domains.terms.dtos.TermsVersionDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

class CacheConfigTest {

    private CacheConfig cacheConfig;

    @BeforeEach
    void setUp() {
        cacheConfig = new CacheConfig();
    }

    @Test
    @DisplayName("Should successfully serialize and deserialize a string token")
    void should_serialize_and_deserialize_string_token() {
        RedisSerializer<String> serializer = new StringRedisSerializer();
        String token = RandomStringUtils.secure().nextAlphanumeric(20);

        byte[] serialized = serializer.serialize(token);

        assertThat(serialized).isNotNull();
        assertThat(serializer.deserialize(serialized)).isEqualTo(token);
    }

    @Test
    @DisplayName("Should successfully serialize and deserialize terms values")
    void should_serialize_and_deserialize_terms_values() {
        RedisSerializer<TermsVersionDto> serializer = cacheConfig.termsSerializer(objectMapper());
        final var terms = TermsBuilder.validTermsVersionDto().build();

        byte[] serialized = serializer.serialize(terms);
        assertThat(serialized).isNotNull();

        assertThat(serializer.deserialize(serialized)).isEqualTo(terms);
    }

    @Test
    @DisplayName("Should handle invalid terms cache values")
    void should_handle_invalid_terms_values() {
        RedisSerializer<TermsVersionDto> serializer = cacheConfig.termsSerializer(objectMapper());

        assertThatThrownBy(() -> serializer.deserialize("invalid-json".getBytes()))
            .isInstanceOf(SerializationException.class);
        assertThat(serializer.deserialize(null)).isNull();
        assertThat(serializer.deserialize(new byte[0])).isNull();
    }

    @Test
    @DisplayName("Should reject unexpected types in terms cache values")
    void should_reject_unexpected_types_in_terms_values() throws Exception {
        RedisSerializer<TermsVersionDto> serializer = cacheConfig.termsSerializer(objectMapper());
        byte[] unexpectedType = objectMapper().writeValueAsBytes(
            java.util.Map.of("@class", "com.buddy.api.domains.account.dtos.AccountDto"));

        assertThatThrownBy(() -> serializer.deserialize(unexpectedType))
            .isInstanceOf(SerializationException.class);
    }

    private ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
