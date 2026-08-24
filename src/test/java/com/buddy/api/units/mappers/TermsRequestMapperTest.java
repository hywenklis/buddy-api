package com.buddy.api.units.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.http.HttpRequestExtractor;
import com.buddy.api.domains.authentication.dtos.AuthenticatedUser;
import com.buddy.api.domains.terms.dtos.AcceptTermsDto;
import com.buddy.api.domains.terms.dtos.CreateTermsVersionDto;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import com.buddy.api.web.terms.mappers.TermsRequestMapper;
import com.buddy.api.web.terms.mappers.TermsRequestMapperImpl;
import com.buddy.api.web.terms.requests.CreateTermsVersionRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;

class TermsRequestMapperTest extends UnitTestAbstract {

    @Mock
    private HttpRequestExtractor httpRequestExtractor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private AuthenticatedUser authUser;

    @Mock
    private UserDetails genericUser;

    private TermsRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TermsRequestMapperImpl(httpRequestExtractor);
    }

    @Test
    @DisplayName("Should map to AcceptTermsDto using authenticated user and extracted request metadata")
    void should_map_to_accept_terms_dto_from_authenticated_user() {
        final String expectedEmail = RandomEmailUtils.generateValidEmail();
        when(authUser.getEmail()).thenReturn(expectedEmail);
        when(httpRequestExtractor.extractIp(request)).thenReturn("192.0.2.10");
        when(httpRequestExtractor.extractUserAgent(request)).thenReturn("Mozilla/5.0");

        AcceptTermsDto result = mapper.toDto(request, authUser);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(expectedEmail);
        assertThat(result.ipAddress()).isEqualTo("192.0.2.10");
        assertThat(result.userAgent()).isEqualTo("Mozilla/5.0");
    }

    @Test
    @DisplayName("Should fallback to getUsername when principal is generic UserDetails")
    void should_fallback_to_username_when_generic_userdetails() {
        final String expectedUsername = RandomStringUtils.secure().nextAlphabetic(10);
        when(genericUser.getUsername()).thenReturn(expectedUsername);

        AcceptTermsDto result = mapper.toDto(request, genericUser);

        assertThat(result.email()).isEqualTo(expectedUsername);
    }

    @Test
    @DisplayName("Should return null email when UserDetails is null")
    void should_return_null_email_when_userdetails_is_null() {
        AcceptTermsDto result = mapper.toDto(request, null);
        assertThat(result.email()).isNull();
    }

    @Test
    @DisplayName("Should return null when both request and userDetails are null")
    void should_return_null_when_both_inputs_null() {
        assertThat(mapper.toDto(null, null)).isNull();
    }

    @Test
    @DisplayName("Should map CreateTermsVersionRequest and UserDetails to CreateTermsVersionDto")
    void should_map_create_terms_version_dto() {
        final String expectedEmail = RandomEmailUtils.generateValidEmail();
        when(authUser.getEmail()).thenReturn(expectedEmail);
        CreateTermsVersionRequest createRequest = new CreateTermsVersionRequest("v1.0", "Terms content", true);

        CreateTermsVersionDto result = mapper.toCreateDto(createRequest, authUser);

        assertThat(result).isNotNull();
        assertThat(result.versionTag()).isEqualTo("v1.0");
        assertThat(result.content()).isEqualTo("Terms content");
        assertThat(result.isActive()).isTrue();
        assertThat(result.publishedByAccountEmail()).isEqualTo(expectedEmail);
    }

    @Test
    @DisplayName("Should return null CreateTermsVersionDto when inputs are null")
    void should_return_null_create_terms_version_dto_when_null() {
        assertThat(mapper.toCreateDto(null, null)).isNull();
    }
}
