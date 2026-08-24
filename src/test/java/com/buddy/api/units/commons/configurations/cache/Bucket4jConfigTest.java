package com.buddy.api.units.commons.configurations.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.cache.Bucket4jConfig;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

class Bucket4jConfigTest {

    private final Bucket4jConfig config = new Bucket4jConfig();

    @Test
    @DisplayName("Should create a Redis client from a Lettuce connection factory")
    void should_create_redis_client() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory("localhost", 6379);

        RedisClient client = config.bucket4jRedisClient(factory);

        assertThat(client).isNotNull();
        client.shutdown();
    }

    @Test
    @DisplayName("Should create a Redis client with password authentication")
    void should_create_redis_client_with_password() {
        var redisConfig = new RedisStandaloneConfiguration("localhost", 6379);
        redisConfig.setPassword(RedisPassword.of("secret"));
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig);

        RedisClient client = config.bucket4jRedisClient(factory);

        assertThat(client).isNotNull();
        client.shutdown();
    }

    @Test
    @DisplayName("Should create a Redis client with username and password authentication")
    void should_create_redis_client_with_username_and_password() {
        var redisConfig = new RedisStandaloneConfiguration("localhost", 6379);
        redisConfig.setUsername("buddyuser");
        redisConfig.setPassword(RedisPassword.of("secret"));
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig);

        RedisClient client = config.bucket4jRedisClient(factory);

        assertThat(client).isNotNull();
        client.shutdown();
    }

    @Test
    @DisplayName("Should create a Redis client with client name configuration")
    void should_create_redis_client_with_client_name() {
        var clientConfig = LettuceClientConfiguration.builder()
            .clientName("buddy-client")
            .build();
        var redisConfig = new RedisStandaloneConfiguration("localhost", 6379);
        LettuceConnectionFactory factory =
            new LettuceConnectionFactory(redisConfig, clientConfig);

        RedisClient client = config.bucket4jRedisClient(factory);

        assertThat(client).isNotNull();
        client.shutdown();
    }

    @Test
    @DisplayName("Should reject unsupported Redis connection factories")
    void should_reject_unsupported_factory() {
        assertThatThrownBy(() -> config.bucket4jRedisClient(mock(RedisConnectionFactory.class)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only LettuceConnectionFactory is supported for Bucket4j");
    }

    @Test
    @DisplayName("Should create a byte-array Redis connection")
    void should_create_redis_connection() {
        RedisClient client = mock(RedisClient.class);
        @SuppressWarnings("unchecked")
        StatefulRedisConnection<byte[], byte[]> expected = mock(StatefulRedisConnection.class);
        when(client.connect(io.lettuce.core.codec.ByteArrayCodec.INSTANCE)).thenReturn(expected);

        StatefulRedisConnection<byte[], byte[]> connection = config.bucket4jRedisConnection(client);

        assertThat(connection).isSameAs(expected);
    }

    @Test
    @DisplayName("Should create a distributed proxy manager")
    void should_create_proxy_manager() {
        @SuppressWarnings("unchecked")
        StatefulRedisConnection<byte[], byte[]> connection = mock(StatefulRedisConnection.class);
        when(connection.async()).thenReturn(mock(RedisAsyncCommands.class));

        assertThat(config.proxyManager(connection)).isNotNull();
    }
}
