package com.buddy.api.units.web.accounts.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.http.HttpRequestExtractor;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.password.dtos.ChangePasswordDto;
import com.buddy.api.domains.authentication.dtos.AuthenticatedUser;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.web.accounts.mappers.ChangePasswordMapperRequest;
import com.buddy.api.web.accounts.requests.ChangePasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class ChangePasswordMapperRequestTest extends UnitTestAbstract {

    @Mock
    private HttpRequestExtractor httpRequestExtractor;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private TestMapper mapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mapper, "httpRequestExtractor", httpRequestExtractor);
    }

    @Test
    @DisplayName("Should extract email and account ID from an authenticated user")
    void should_extract_authenticated_user_data() {
        UUID accountId = UUID.randomUUID();
        String email = com.buddy.api.utils.RandomEmailUtils.generateValidEmail();
        AccountDto account = AccountDto.builder().accountId(accountId)
            .email(new com.buddy.api.domains.valueobjects.EmailAddress(email))
            .password("password")
            .isBlocked(false)
            .isDeleted(false)
            .build();
        AuthenticatedUser user = new AuthenticatedUser(account,
            List.of(new SimpleGrantedAuthority("USER")));

        assertThat(mapper.extractEmail(user)).isEqualTo(user.getEmail());
        assertThat(mapper.extractAccountId(user)).isEqualTo(accountId);
    }

    @Test
    @DisplayName("Should extract username and no account ID from generic user details")
    void should_extract_generic_user_data() {
        var user = org.springframework.security.core.userdetails.User.withUsername(
                com.buddy.api.utils.RandomEmailUtils.generateValidEmail()).password("password")
            .authorities("USER").build();

        assertThat(mapper.extractEmail(user)).isEqualTo(user.getUsername());
        assertThat(mapper.extractAccountId(user)).isNull();
        assertThat(mapper.extractEmail(null)).isNull();
    }

    @Test
    @DisplayName("Should delegate request metadata extraction")
    void should_extract_request_metadata() {
        when(httpRequestExtractor.extractIp(request)).thenReturn("127.0.0.1");
        when(httpRequestExtractor.extractUserAgent(request)).thenReturn("agent");

        assertThat(mapper.extractIp(request)).isEqualTo("127.0.0.1");
        assertThat(mapper.extractUserAgent(request)).isEqualTo("agent");
    }

    static class TestMapper extends ChangePasswordMapperRequest {
        @Override
        public ChangePasswordDto toDto(
            final ChangePasswordRequest body,
            final HttpServletRequest request,
            final UserDetails userDetails
        ) {
            return null;
        }
    }
}
