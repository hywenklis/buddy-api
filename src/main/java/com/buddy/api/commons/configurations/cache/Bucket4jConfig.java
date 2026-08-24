package com.buddy.api.commons.configurations.cache;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class Bucket4jConfig {

    @Bean(destroyMethod = "shutdown")
    public RedisClient bucket4jRedisClient(final RedisConnectionFactory connectionFactory) {
        if (connectionFactory instanceof LettuceConnectionFactory lettuceConnectionFactory) {
            RedisStandaloneConfiguration redisConfiguration =
                lettuceConnectionFactory.getStandaloneConfiguration();
            var clientConfiguration = lettuceConnectionFactory.getClientConfiguration();
            RedisURI.Builder uriBuilder = RedisURI.builder()
                .withHost(redisConfiguration.getHostName())
                .withPort(redisConfiguration.getPort())
                .withDatabase(redisConfiguration.getDatabase())
                .withSsl(clientConfiguration.isUseSsl())
                .withVerifyPeer(clientConfiguration.getVerifyMode())
                .withStartTls(clientConfiguration.isStartTls())
                .withTimeout(clientConfiguration.getCommandTimeout());

            clientConfiguration.getClientName().ifPresent(uriBuilder::withClientName);
            RedisPassword password = redisConfiguration.getPassword();
            if (password.isPresent()) {
                String username = redisConfiguration.getUsername();
                if (username == null) {
                    uriBuilder.withPassword(password.get());
                } else {
                    uriBuilder.withAuthentication(username, password.get());
                }
            }

            return RedisClient.create(uriBuilder.build());
        }
        throw new IllegalStateException("Only LettuceConnectionFactory is supported for Bucket4j");
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<byte[], byte[]> bucket4jRedisConnection(
        final RedisClient redisClient
    ) {
        return redisClient.connect(io.lettuce.core.codec.ByteArrayCodec.INSTANCE);
    }

    @Bean
    public ProxyManager<byte[]> proxyManager(
        final StatefulRedisConnection<byte[], byte[]> connection
    ) {
        return LettuceBasedProxyManager.builderFor(connection.async()).build();
    }
}
