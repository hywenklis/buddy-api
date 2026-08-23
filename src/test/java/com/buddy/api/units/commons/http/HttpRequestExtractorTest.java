package com.buddy.api.units.commons.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.http.HttpRequestExtractor;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class HttpRequestExtractorTest {

    @Mock
    private HttpServletRequest request;

    private HttpRequestExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new HttpRequestExtractor();
    }

    @Test
    @DisplayName("Should use remote address when request is not from a trusted proxy")
    void should_use_remote_address_for_untrusted_request() {
        when(request.getRemoteAddr()).thenReturn("192.0.2.10");

        assertThat(extractor.extractIp(request)).isEqualTo("192.0.2.10");
    }

    @Test
    @DisplayName("Should use forwarded address when request is from a trusted proxy")
    void should_use_forwarded_address_for_trusted_proxy() {
        ReflectionTestUtils.setField(extractor, "trustedProxyAddresses", List.of("192.0.2.10"));
        when(request.getRemoteAddr()).thenReturn("192.0.2.10");
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.20, 203.0.113.5");

        assertThat(extractor.extractIp(request)).isEqualTo("198.51.100.20");
    }
}
