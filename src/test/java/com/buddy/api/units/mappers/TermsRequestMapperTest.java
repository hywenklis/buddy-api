package com.buddy.api.units.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.buddy.api.domains.authentication.dtos.AuthenticatedUser;
import com.buddy.api.domains.terms.dtos.AcceptTermsDto;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import com.buddy.api.web.terms.mappers.TermsRequestMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;

class TermsRequestMapperTest extends UnitTestAbstract {

    @Mock
    private HttpServletRequest request;

    @Mock
    AuthenticatedUser authUser;

    @Mock
    UserDetails genericUser;

    private final TermsRequestMapper mapper = Mappers.getMapper(TermsRequestMapper.class);

    @Test
    @DisplayName("Should use email from AuthenticatedUser "
        + "when principal is instance of AuthenticatedUser")
    void should_use_email_from_authenticated_user() {
        final String expectedEmail = RandomEmailUtils.generateValidEmail();
        when(authUser.getEmail()).thenReturn(expectedEmail);
        AcceptTermsDto result = mapper.toDto(request, authUser);
        assertThat(result.email()).isEqualTo(expectedEmail);
    }

    @Test
    @DisplayName("Should fallback to getUsername when principal is NOT AuthenticatedUser")
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
}
