package com.buddy.api.integrations.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.commons.configurations.security.jwt.TokenBlocklistService;
import com.buddy.api.integrations.IntegrationTestAbstract;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("Token Blocklist Service Retry and Resilience Integration Tests")
class TokenBlocklistServiceRetryIntegrationTest extends IntegrationTestAbstract {

    @Autowired
    private TokenBlocklistService tokenBlocklistService;

    @Test
    @DisplayName("Should successfully block token and verify in Redis")
    void should_block_token_successfully() {
        final var token = "sample-jwt-token-to-block";

        tokenBlocklistService.blockToken(token, 3600);

        final var isBlocked = tokenBlocklistService.isBlocked(token);
        assertThat(isBlocked).isTrue();
    }

    @Test
    @DisplayName("Should successfully revoke all user tokens and verify in Redis")
    void should_revoke_all_user_tokens_successfully() {
        final var email = "resilience-test@example.com";
        final var issuedAt = Instant.now().toEpochMilli() - 10000L;

        tokenBlocklistService.revokeAllUserTokens(email);

        final var isRevoked = tokenBlocklistService.isUserTokensRevoked(email, issuedAt);
        assertThat(isRevoked).isTrue();
    }

    @Test
    @DisplayName("Should report not revoked when issued after revocation")
    void should_report_not_revoked_when_issued_after_revocation() {
        final var email = "resilience-future-test@example.com";

        tokenBlocklistService.revokeAllUserTokens(email);

        final var issuedAtFuture = Instant.now().toEpochMilli() + 50000L;
        final var isRevoked = tokenBlocklistService.isUserTokensRevoked(email, issuedAtFuture);
        assertThat(isRevoked).isFalse();
    }
}
