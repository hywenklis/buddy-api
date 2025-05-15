package com.buddy.api.units.commons.configurations.secutiry.cookies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.properties.AuthProperties;
import com.buddy.api.commons.configurations.security.cookies.CookieFactory;
import com.buddy.api.units.UnitTestAbstract;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class CookieFactoryTest extends UnitTestAbstract {

    @Mock
    private AuthProperties authProperties;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CookieFactory cookieFactory;

    @Test
    @DisplayName("Should add authentication cookies with correct attributes")
    void addAuthCookies_addsCookiesCorrectly() {
        when(authProperties.getAccessTokenExpirationInSeconds()).thenReturn(ONE_HOUR_IN_SECONDS);
        when(authProperties.getRefreshTokenExpirationInSeconds()).thenReturn(SEVEN_DAYS_IN_SECONDS);

        cookieFactory.addAuthCookies(response, ACCESS_TOKEN, REFRESH_TOKEN);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookieCaptor.capture());

        var cookies = cookieCaptor.getAllValues();
        assertThat(cookies).hasSize(2);

        Cookie accessCookie = cookies.get(0);
        assertThat(accessCookie.getName()).isEqualTo(ACCESS_TOKEN_NAME);
        assertThat(accessCookie.getValue()).isEqualTo(ACCESS_TOKEN);
        assertThat(accessCookie.getMaxAge()).isEqualTo(ONE_HOUR_IN_SECONDS);
        assertThat(accessCookie.isHttpOnly()).isTrue();
        assertThat(accessCookie.getSecure()).isTrue();
        assertThat(accessCookie.getPath()).isEqualTo("/");

        Cookie refreshCookie = cookies.get(1);
        assertThat(refreshCookie.getName()).isEqualTo(REFRESH_TOKEN_NAME);
        assertThat(refreshCookie.getValue()).isEqualTo(REFRESH_TOKEN);
        assertThat(refreshCookie.getMaxAge()).isEqualTo(SEVEN_DAYS_IN_SECONDS);
        assertThat(refreshCookie.isHttpOnly()).isTrue();
        assertThat(refreshCookie.getSecure()).isTrue();
        assertThat(refreshCookie.getPath()).isEqualTo("/");

        verify(response).addHeader(SET_COOKIE_HEADER,
            String.format("%s=%s; Max-Age=%d; %s", ACCESS_TOKEN_NAME, ACCESS_TOKEN,
                ONE_HOUR_IN_SECONDS,
                COOKIE_ATTRIBUTES));
        verify(response).addHeader(SET_COOKIE_HEADER,
            String.format("%s=%s; Max-Age=%d; %s", REFRESH_TOKEN_NAME, REFRESH_TOKEN,
                SEVEN_DAYS_IN_SECONDS,
                COOKIE_ATTRIBUTES));
    }

    @Test
    @DisplayName("Should invalidate authentication cookies by setting max age to 0")
    void invalidateAuthCookies_invalidatesCookiesCorrectly() {
        cookieFactory.invalidateAuthCookies(response);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookieCaptor.capture());

        var cookies = cookieCaptor.getAllValues();
        assertThat(cookies).hasSize(2);

        Cookie accessCookie = cookies.get(0);
        assertThat(accessCookie.getName()).isEqualTo(ACCESS_TOKEN_NAME);
        assertThat(accessCookie.getValue()).isNull();
        assertThat(accessCookie.getMaxAge()).isZero();
        assertThat(accessCookie.isHttpOnly()).isTrue();
        assertThat(accessCookie.getSecure()).isTrue();
        assertThat(accessCookie.getPath()).isEqualTo("/");

        Cookie refreshCookie = cookies.get(1);
        assertThat(refreshCookie.getName()).isEqualTo(REFRESH_TOKEN_NAME);
        assertThat(refreshCookie.getValue()).isNull();
        assertThat(refreshCookie.getMaxAge()).isZero();
        assertThat(refreshCookie.isHttpOnly()).isTrue();
        assertThat(refreshCookie.getSecure()).isTrue();
        assertThat(refreshCookie.getPath()).isEqualTo("/");

        verify(response).addHeader(SET_COOKIE_HEADER,
            String.format("%s=; Max-Age=0; %s", ACCESS_TOKEN_NAME, COOKIE_ATTRIBUTES));
        verify(response).addHeader(SET_COOKIE_HEADER,
            String.format("%s=; Max-Age=0; %s", REFRESH_TOKEN_NAME, COOKIE_ATTRIBUTES));
    }
}
