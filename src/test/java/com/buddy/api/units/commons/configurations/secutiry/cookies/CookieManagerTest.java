package com.buddy.api.units.commons.configurations.secutiry.cookies;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.buddy.api.commons.configurations.security.cookies.CookieFactory;
import com.buddy.api.commons.configurations.security.cookies.CookieManager;
import com.buddy.api.units.UnitTestAbstract;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class CookieManagerTest extends UnitTestAbstract {

    @Mock
    private CookieFactory cookieFactory;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CookieManager cookieManager;

    @Test
    @DisplayName("Should add authentication cookies on handleCookies")
    void handleCookies_addsAuthCookies() {
        cookieManager.handleCookies(request, response, ACCESS_TOKEN, REFRESH_TOKEN);

        verify(cookieFactory).addAuthCookies(response, ACCESS_TOKEN, REFRESH_TOKEN);
        verifyNoMoreInteractions(cookieFactory);
    }

    @Test
    @DisplayName("Should clear authentication cookies on clearCookies")
    void clearCookies_invalidatesAuthCookies() {
        cookieManager.clearCookies(response);

        verify(cookieFactory).invalidateAuthCookies(response);
        verifyNoMoreInteractions(cookieFactory);
    }
}

