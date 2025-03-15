package com.buddy.api.units.commons.configurations.secutiry.origin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.properties.AuthProperties;
import com.buddy.api.commons.configurations.security.origin.ClientTypeDetector;
import com.buddy.api.commons.configurations.security.origin.enums.ClientType;
import com.buddy.api.units.UnitTestAbstract;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class ClientTypeDetectorTest extends UnitTestAbstract {

    @Mock
    private AuthProperties authProperties;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ClientTypeDetector clientTypeDetector;

    @Test
    @DisplayName("Should return UNKNOWN when Origin header is null")
    void should_return_unknown_when_origin_is_null() {
        when(request.getHeader(ORIGIN_NAME)).thenReturn(null);

        ClientType result = clientTypeDetector.detectClientType(request);

        assertThat(result).isEqualTo(ClientType.UNKNOWN);

        verify(request, times(1)).getHeader(ORIGIN_NAME);
        verify(authProperties, times(0)).allowedOrigins();
    }

    @ParameterizedTest
    @MethodSource("provideKnownOrigins")
    @DisplayName("Should return correct client type when Origin matches a known config")
    void should_return_correct_client_type_when_origin_matches(final String origin,
                                                               final ClientType expectedType
    ) {
        List<AuthProperties.OriginConfig> origins = createDefaultOrigins();

        when(request.getHeader(ORIGIN_NAME)).thenReturn(origin);
        when(authProperties.allowedOrigins()).thenReturn(origins);

        ClientType result = clientTypeDetector.detectClientType(request);

        assertThat(result).isEqualTo(expectedType);

        verify(request, times(1)).getHeader(ORIGIN_NAME);
        verify(authProperties, times(1)).allowedOrigins();
    }

    @Test
    @DisplayName("Should return UNKNOWN when Origin does not match any config")
    void should_return_unknown_when_origin_does_not_match() {
        String origin = UUID.randomUUID().toString();

        when(request.getHeader(ORIGIN_NAME)).thenReturn(origin);

        ClientType result = clientTypeDetector.detectClientType(request);

        assertThat(result).isEqualTo(ClientType.UNKNOWN);

        verify(request, times(1)).getHeader(ORIGIN_NAME);
        verify(authProperties, times(1)).allowedOrigins();
    }

    private List<AuthProperties.OriginConfig> createDefaultOrigins() {
        var webOrigin = AuthProperties.OriginConfig.builder()
            .type(ClientType.WEB.name())
            .code("550e8400-e29b-41d4-a716-446655440000")
            .build();

        var mobileOrigin = AuthProperties.OriginConfig.builder()
            .type(ClientType.MOBILE.name())
            .code("123e4567-e89b-12d3-a456-426614174000")
            .build();

        var toolsOrigin = AuthProperties.OriginConfig.builder()
            .type(ClientType.TOOLS.name())
            .code("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
            .build();

        return List.of(webOrigin, mobileOrigin, toolsOrigin);
    }

    private static Stream<Arguments> provideKnownOrigins() {
        return Stream.of(
            Arguments.of("550e8400-e29b-41d4-a716-446655440000", ClientType.WEB),
            Arguments.of("123e4567-e89b-12d3-a456-426614174000", ClientType.MOBILE),
            Arguments.of("6ba7b810-9dad-11d1-80b4-00c04fd430c8", ClientType.TOOLS)
        );
    }
}
