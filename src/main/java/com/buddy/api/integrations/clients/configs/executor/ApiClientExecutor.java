package com.buddy.api.integrations.clients.configs.executor;

import com.buddy.api.commons.exceptions.ManagerApiException;
import com.buddy.api.integrations.clients.configs.exceptions.ClientBadRequestException;
import com.buddy.api.integrations.clients.configs.exceptions.ClientUnauthorizedException;
import com.buddy.api.integrations.clients.configs.exceptions.GenericClientException;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApiClientExecutor {

    public <T> T execute(final String integrationName, final Supplier<T> action) {
        try {
            return action.get();
        } catch (final ClientUnauthorizedException e) {
            log.warn("Authentication failure in [{}]. Status: {}, Body: {}",
                integrationName, e.getStatus(), e.getErrorBody());
            throw new ManagerApiException(
                "Authentication failed for external service: " + integrationName,
                "integration.credentials",
                HttpStatus.valueOf(e.getStatus()), e
            );

        } catch (final ClientBadRequestException e) {
            log.warn("Invalid request to [{}]. Status: {}, Body: {}",
                integrationName, e.getStatus(), e.getErrorBody());
            throw new ManagerApiException(
                "Invalid request for external service: " + integrationName,
                "integration.request",
                HttpStatus.valueOf(e.getStatus()), e
            );

        } catch (final GenericClientException e) {
            log.error("Generic client error in [{}]. Status: {}, Body: {}",
                integrationName, e.getStatus(), e.getErrorBody(), e);
            throw new ManagerApiException(
                "Error in external service: " + integrationName,
                "integration.error",
                HttpStatus.valueOf(e.getStatus()), e
            );

        } catch (final Exception e) {
            log.error("Unexpected error during [{}] integration call.", integrationName, e);
            throw new ManagerApiException(
                "Unexpected error while communicating with external service: " + integrationName,
                "integration.unexpected",
                HttpStatus.INTERNAL_SERVER_ERROR, e
            );
        }
    }
}
