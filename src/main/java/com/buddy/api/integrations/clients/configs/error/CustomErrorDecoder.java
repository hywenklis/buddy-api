package com.buddy.api.integrations.clients.configs.error;

import com.buddy.api.commons.exceptions.ReadBodyIoException;
import com.buddy.api.integrations.clients.configs.exceptions.ClientBadRequestException;
import com.buddy.api.integrations.clients.configs.exceptions.ClientNotFoundException;
import com.buddy.api.integrations.clients.configs.exceptions.ClientUnauthorizedException;
import com.buddy.api.integrations.clients.configs.exceptions.GenericClientException;
import feign.Response;
import feign.codec.ErrorDecoder;
import io.micrometer.core.instrument.util.IOUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(final String methodKey, final Response response) {
        String responseBody;
        try {
            responseBody = readErrorBody(response);
        } catch (IOException e) {
            return new ReadBodyIoException(
                "Failed to read error response body for method: " + methodKey,
                "responseBody",
                e.getCause()
            );
        }

        log.error("Error calling external service: methodKey={}, status={}, responseBody={}",
            methodKey, response.status(), responseBody);

        final String errorMessage = "Error calling external service: " + methodKey;

        return switch (response.status()) {
            case 400 ->
                new ClientBadRequestException(errorMessage, responseBody, response.status());
            case 401, 403 ->
                new ClientUnauthorizedException(errorMessage, responseBody, response.status());
            case 404 -> new ClientNotFoundException(errorMessage, responseBody, response.status());
            default -> new GenericClientException(errorMessage, responseBody, response.status());
        };
    }

    private String readErrorBody(final Response response) throws IOException {
        if (response.body() == null) {
            return "No response body available";
        }
        return IOUtils.toString(response.body().asInputStream(), StandardCharsets.UTF_8);
    }
}
