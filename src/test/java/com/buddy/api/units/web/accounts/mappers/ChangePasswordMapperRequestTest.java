package com.buddy.api.units.web.accounts.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.http.HttpRequestExtractor;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.password.dtos.ChangePasswordDto;
import com.buddy.api.domains.authentication.dtos.AuthenticatedUser;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.web.accounts.mappers.ChangePasswordMapperRequest;
import com.buddy.api.web.accounts.mappers.ChangePasswordMapperRequestImpl;
import com.buddy.api.web.accounts.requests.ChangePasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class ChangePasswordMapperRequestTest extends UnitTestAbstract {

    @Mock
    private HttpRequestExtractor httpRequestExtractor;

    @Mock
    private HttpServletRequest request;

    private ChangePasswordMapperRequest mapper;

    @BeforeEach
    void setUp() {
        mapper = new ChangePasswordMapperRequestImpl(httpRequestExtractor);
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
        assertThat(mapper.extractAccountId(null)).isNull();
    }

    @Test
    @DisplayName("Should map ChangePasswordRequest to ChangePasswordDto using extractor")
    void should_map_change_password_request_to_dto() {
        UUID accountId = UUID.randomUUID();
        String email = com.buddy.api.utils.RandomEmailUtils.generateValidEmail();
        AccountDto account = AccountDto.builder().accountId(accountId)
            .email(new com.buddy.api.domains.valueobjects.EmailAddress(email))
            .password("oldPassword")
            .isBlocked(false)
            .isDeleted(false)
            .build();
        AuthenticatedUser user = new AuthenticatedUser(account,
            List.of(new SimpleGrantedAuthority("USER")));

        ChangePasswordRequest requestBody = new ChangePasswordRequest(
            "OldPassword123!",
            "NewPassword123!"
        );

        when(httpRequestExtractor.extractIp(request)).thenReturn("127.0.0.1");
        when(httpRequestExtractor.extractUserAgent(request)).thenReturn("Mozilla/5.0");

        ChangePasswordDto dto = mapper.toDto(requestBody, request, user);

        assertThat(dto).isNotNull();
        assertThat(dto.accountId()).isEqualTo(accountId);
        assertThat(dto.email()).isEqualTo(user.getEmail());
        assertThat(dto.currentPassword()).isEqualTo("OldPassword123!");
        assertThat(dto.newPassword()).isEqualTo("NewPassword123!");
        assertThat(dto.ipAddress()).isEqualTo("127.0.0.1");
        assertThat(dto.userAgent()).isEqualTo("Mozilla/5.0");
    }

    @Test
    @DisplayName("Should return null when all inputs are null")
    void should_return_null_when_all_inputs_null() {
        assertThat(mapper.toDto(null, null, null)).isNull();
    }
}
