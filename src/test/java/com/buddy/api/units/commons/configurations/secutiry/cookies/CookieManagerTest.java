package com.buddy.api.units.commons.configurations.secutiry.cookies;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.security.cookies.CookieFactory;
import com.buddy.api.commons.configurations.security.cookies.CookieManager;
import com.buddy.api.commons.configurations.security.origin.ClientTypeDetector;
import com.buddy.api.commons.configurations.security.origin.enums.ClientType;
import com.buddy.api.commons.exceptions.InvalidClientOriginException;
import com.buddy.api.units.UnitTestAbstract;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class CookieManagerTest extends UnitTestAbstract {

    @Mock
    private ClientTypeDetector clientTypeDetector;

    @Mock
    private CookieFactory cookieFactory;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CookieManager cookieManager;

    @Test
    @DisplayName("Should add authentication cookies when client type is WEB")
    void handleCookies_webClient_addsCookies() {
        when(clientTypeDetector.detectClientType(request)).thenReturn(ClientType.WEB);

        cookieManager.handleCookies(request, response, ACCESS_TOKEN, REFRESH_TOKEN);

        verify(clientTypeDetector).detectClientType(request);
        verify(cookieFactory).addAuthCookies(response, ACCESS_TOKEN, REFRESH_TOKEN);
        verify(cookieFactory, never()).invalidateAuthCookies(response);
        verifyNoMoreInteractions(cookieFactory);
    }

    @Test
    @DisplayName("Should add authentication cookies when client type is TOOLS")
    void handleCookies_toolsClient_addsCookies() {
        when(clientTypeDetector.detectClientType(request)).thenReturn(ClientType.TOOLS);

        cookieManager.handleCookies(request, response, ACCESS_TOKEN, REFRESH_TOKEN);

        verify(clientTypeDetector).detectClientType(request);
        verify(cookieFactory).addAuthCookies(response, ACCESS_TOKEN, REFRESH_TOKEN);
        verify(cookieFactory, never()).invalidateAuthCookies(response);
        verifyNoMoreInteractions(cookieFactory);
    }

    @Test
    @DisplayName("Should invalidate authentication cookies when client type is MOBILE")
    void handleCookies_mobileClient_invalidatesCookies() {
        when(clientTypeDetector.detectClientType(request)).thenReturn(ClientType.MOBILE);

        cookieManager.handleCookies(request, response, ACCESS_TOKEN, REFRESH_TOKEN);

        verify(clientTypeDetector).detectClientType(request);
        verify(cookieFactory).invalidateAuthCookies(response);
        verify(cookieFactory, never()).addAuthCookies(response, ACCESS_TOKEN, REFRESH_TOKEN);
        verifyNoMoreInteractions(cookieFactory);
    }

    @Test
    @DisplayName("Should throw InvalidClientOriginException when client type is UNKNOWN")
    void handleCookies_unknownClient_throwsInvalidOriginException() {
        when(clientTypeDetector.detectClientType(request)).thenReturn(ClientType.UNKNOWN);

        assertThatThrownBy(() -> cookieManager.handleCookies(
            request, response, ACCESS_TOKEN, REFRESH_TOKEN)
        )
            .isInstanceOf(InvalidClientOriginException.class)
            .hasMessage("Invalid origin detected");

        verify(clientTypeDetector).detectClientType(request);
        verify(cookieFactory, never()).addAuthCookies(response, ACCESS_TOKEN, REFRESH_TOKEN);
        verify(cookieFactory, never()).invalidateAuthCookies(response);
        verifyNoMoreInteractions(cookieFactory);
    }

    @Test
    @DisplayName("Should invalidate authentication cookies when client type is not mapped")
    void handleCookies_unmappedClientType_invalidatesCookies() {
        ClientType unmappedType = mock(ClientType.class);
        when(clientTypeDetector.detectClientType(request)).thenReturn(unmappedType);

        cookieManager.handleCookies(request, response, ACCESS_TOKEN, REFRESH_TOKEN);

        verify(clientTypeDetector).detectClientType(request);
        verify(cookieFactory).invalidateAuthCookies(response);
        verify(cookieFactory, never()).addAuthCookies(response, ACCESS_TOKEN, REFRESH_TOKEN);
        verifyNoMoreInteractions(cookieFactory);
    }
}
