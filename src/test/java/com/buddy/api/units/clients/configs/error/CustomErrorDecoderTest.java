package com.buddy.api.units.clients.configs.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.exceptions.ReadyIoException;
import com.buddy.api.integrations.clients.configs.error.CustomErrorDecoder;
import com.buddy.api.integrations.clients.configs.exceptions.ClientBadRequestException;
import com.buddy.api.integrations.clients.configs.exceptions.ClientNotFoundException;
import com.buddy.api.integrations.clients.configs.exceptions.ClientUnauthorizedException;
import com.buddy.api.integrations.clients.configs.exceptions.GenericClientException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("PMD.CloseResource")
@DisplayName("Tests for CustomErrorDecoder")
class CustomErrorDecoderTest {

    private CustomErrorDecoder errorDecoder;
    private Request request;
    private final String methodKey = "TestClient#testMethod()";
    private final String errorBodyJson = "{\"error\":\"test error message\"}";

    @BeforeEach
    void setUp() {
        errorDecoder = new CustomErrorDecoder();
        request = Request.create(
            Request.HttpMethod.GET,
            "/test",
            Collections.emptyMap(),
            null,
            new RequestTemplate()
        );
    }

    @Test
    @DisplayName("Should return ClientBadRequestException for status 400")
    void decode_whenStatus400_shouldReturnClientBadRequestException() throws IOException {
        final Response response = createMockResponse(400, errorBodyJson);
        final Exception exception = errorDecoder.decode(methodKey, response);
        final ClientBadRequestException actualException = (ClientBadRequestException) exception;

        assertThat(exception).isInstanceOf(ClientBadRequestException.class);
        assertThat(actualException.getErrorBody()).isEqualTo(errorBodyJson);
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403})
    @DisplayName("Should return ClientUnauthorizedException for status 401 and 403")
    void decode_whenStatusIsUnauthorized_shouldReturnClientUnauthorizedException(final int status)
        throws IOException {
        final Response response = createMockResponse(status, errorBodyJson);
        final Exception exception = errorDecoder.decode(methodKey, response);

        assertThat(exception).isInstanceOf(ClientUnauthorizedException.class);
    }

    @Test
    @DisplayName("Should return ClientNotFoundException for status 404")
    void decode_whenStatus404_shouldReturnClientNotFoundException() throws IOException {
        final Response response = createMockResponse(404, errorBodyJson);
        final Exception exception = errorDecoder.decode(methodKey, response);

        assertThat(exception).isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    @DisplayName("Should return GenericClientException for other error status codes (e.g., 500)")
    void decode_whenStatusIsOtherError_shouldReturnGenericClientException() throws IOException {
        final Response response = createMockResponse(500, "{\"error\":\"internal server error\"}");
        final Exception exception = errorDecoder.decode(methodKey, response);

        assertThat(exception).isInstanceOf(GenericClientException.class);
    }

    @Test
    @DisplayName("Should return ReadBodyIoException when reading the body fails")
    void decode_whenBodyReadingFails_shouldReturnReadBodyIoException() throws IOException {
        final Response response = mock(Response.class);
        final Response.Body body = mock(Response.Body.class);

        when(response.body()).thenReturn(body);
        when(body.asInputStream()).thenThrow(new IOException("Stream reading failed"));

        final Exception exception = errorDecoder.decode(methodKey, response);

        assertThat(exception)
            .isInstanceOf(ReadyIoException.class)
            .hasMessageContaining("Failed to read error response body");
    }

    @Test
    @DisplayName("Should handle null response body gracefully")
    void decode_whenResponseBodyIsNull_shouldHandleGracefully() throws IOException {
        final Response response = createMockResponse(404, null);

        final Exception exception = errorDecoder.decode(methodKey, response);

        assertThat(exception).isInstanceOf(ClientNotFoundException.class);
        final ClientNotFoundException actualException = (ClientNotFoundException) exception;
        assertThat(actualException.getErrorBody())
            .isEqualTo("No response body available");
    }

    private Response createMockResponse(final int status, final String body) throws IOException {
        Response mockResponse = mock(Response.class);

        when(mockResponse.status()).thenReturn(status);
        when(mockResponse.reason()).thenReturn("Test Reason");
        when(mockResponse.request()).thenReturn(request);
        when(mockResponse.headers()).thenReturn(Collections.emptyMap());

        if (body != null) {
            Response.Body mockBody = mock(Response.Body.class);
            when(mockResponse.body()).thenReturn(mockBody);
            when(mockBody.asInputStream()).thenReturn(
                new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8))
            );
        } else {
            when(mockResponse.body()).thenReturn(null);
        }

        return mockResponse;
    }
}
