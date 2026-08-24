package com.buddy.api.units.commons.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.http.ClientIpResolver;
import com.buddy.api.units.UnitTestAbstract;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClientIpResolverTest extends UnitTestAbstract {

    private static final String UNKNOWN_IP = "0.0.0.0";
    private static final String REMOTE_ADDR = "192.168.1.100";
    private static final String TRUSTED_PROXY = "10.0.0.1";
    private static final String CLIENT_IP = "203.0.113.195";

    @Test
    @DisplayName("Should return unknown IP when request is null")
    void shouldReturnUnknownIpWhenRequestIsNull() {
        String ip = ClientIpResolver.extract(null, List.of(TRUSTED_PROXY));

        assertThat(ip).isEqualTo(UNKNOWN_IP);
    }

    @Test
    @DisplayName("Should return remote address when proxy is not trusted")
    void shouldReturnRemoteAddressWhenProxyIsNotTrusted() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(REMOTE_ADDR);

        String ip = ClientIpResolver.extract(request, List.of(TRUSTED_PROXY));

        assertThat(ip).isEqualTo(REMOTE_ADDR);
    }

    @Test
    @DisplayName("Should return unknown IP when remote address is blank and proxy is not trusted")
    void shouldReturnUnknownIpWhenRemoteAddressIsBlank() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("   ");

        String ip = ClientIpResolver.extract(request, List.of(TRUSTED_PROXY));

        assertThat(ip).isEqualTo(UNKNOWN_IP);
    }

    @Test
    @DisplayName("Should extract first IP from X-Forwarded-For when proxy is trusted")
    void shouldExtractFirstIpFromForwardedHeaderWhenProxyIsTrusted() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(TRUSTED_PROXY);
        when(request.getHeader("X-Forwarded-For")).thenReturn(CLIENT_IP + ", 198.51.100.1");

        String ip = ClientIpResolver.extract(request, List.of(TRUSTED_PROXY));

        assertThat(ip).isEqualTo(CLIENT_IP);
    }

    @Test
    @DisplayName("Should fallback to X-Real-IP when X-Forwarded-For is absent and proxy is trusted")
    void shouldFallbackToRealIpHeaderWhenForwardedIsAbsentAndProxyIsTrusted() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(TRUSTED_PROXY);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(CLIENT_IP);

        String ip = ClientIpResolver.extract(request, List.of(TRUSTED_PROXY));

        assertThat(ip).isEqualTo(CLIENT_IP);
    }

    @Test
    @DisplayName("Should fallback to remote address when headers are blank and proxy is trusted")
    void shouldFallbackToRemoteAddressWhenHeadersAreBlankAndProxyIsTrusted() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(TRUSTED_PROXY);
        when(request.getHeader("X-Forwarded-For")).thenReturn("  ");
        when(request.getHeader("X-Real-IP")).thenReturn(null);

        String ip = ClientIpResolver.extract(request, List.of(TRUSTED_PROXY));

        assertThat(ip).isEqualTo(TRUSTED_PROXY);
    }

    @Test
    @DisplayName("Should return remote address when trusted proxy list is null")
    void shouldReturnRemoteAddressWhenTrustedProxyListIsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(REMOTE_ADDR);

        String ip = ClientIpResolver.extract(request, null);

        assertThat(ip).isEqualTo(REMOTE_ADDR);
    }
}
