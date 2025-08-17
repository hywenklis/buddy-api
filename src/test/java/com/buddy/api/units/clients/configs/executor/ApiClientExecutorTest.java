package com.buddy.api.units.clients.configs.executor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.buddy.api.commons.exceptions.ManagerApiException;
import com.buddy.api.integrations.clients.configs.exceptions.ClientBadRequestException;
import com.buddy.api.integrations.clients.configs.exceptions.ClientUnauthorizedException;
import com.buddy.api.integrations.clients.configs.exceptions.GenericClientException;
import com.buddy.api.integrations.clients.configs.executor.ApiClientExecutor;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("Unit Tests for ApiClientExecutor")
class ApiClientExecutorTest {

    private ApiClientExecutor apiClientExecutor;
    private final String integrationName = "Test Integration name";

    @BeforeEach
    void setUp() {
        apiClientExecutor = new ApiClientExecutor();
    }

    @Nested
    @DisplayName("Success Scenarios")
    class SuccessScenarios {

        @Test
        @DisplayName("should return the result of the action when it executes successfully")
        void execute_whenActionSucceeds_shouldReturnValue() {
            final String expectedResult = "Success";
            Supplier<String> successfulAction = () -> expectedResult;

            String actualResult = apiClientExecutor.execute(integrationName, successfulAction);

            assertThat(actualResult).isEqualTo(expectedResult);
        }
    }

    @Nested
    @DisplayName("Failure Scenarios")
    class FailureScenarios {

        @Test
        @DisplayName("should catch ClientUnauthorizedException and throw ManagerApiException")
        void execute_whenActionThrowsClientUnauthorizedException_shouldThrowManagerApiException() {
            final var cause = new ClientUnauthorizedException("Auth Error", "{}", 401);
            Supplier<Void> failingAction = () -> {
                throw cause;
            };

            assertThatThrownBy(() -> apiClientExecutor.execute(integrationName, failingAction))
                .isInstanceOf(ManagerApiException.class)
                .hasMessage("Authentication failed for external service: " + integrationName)
                .satisfies(ex -> {
                    final var managerApiException = (ManagerApiException) ex;

                    assertThat(managerApiException.getFieldName())
                        .isEqualTo("integration.credentials");

                    assertThat(managerApiException.getHttpStatus())
                        .isEqualTo(HttpStatus.UNAUTHORIZED);
                });
        }

        @Test
        @DisplayName("should catch ClientBadRequestException and throw ManagerApiException")
        void execute_whenActionThrowsClientBadRequestException_shouldThrowManagerApiException() {
            final var cause = new ClientBadRequestException("Bad Request", "{}", 400);
            Supplier<Void> failingAction = () -> {
                throw cause;
            };

            assertThatThrownBy(() -> apiClientExecutor.execute(integrationName, failingAction))
                .isInstanceOf(ManagerApiException.class)
                .hasMessage("Invalid request for external service: " + integrationName)
                .satisfies(ex -> {
                    final var managerApiException = (ManagerApiException) ex;

                    assertThat(managerApiException.getFieldName())
                        .isEqualTo("integration.request");

                    assertThat(managerApiException.getHttpStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST);
                });
        }

        @Test
        @DisplayName("should catch GenericClientException and throw ManagerApiException")
        void execute_whenActionThrowsGenericClientException_shouldThrowManagerApiException() {
            final var cause = new GenericClientException("Server Error", "{}", 503);
            Supplier<Void> failingAction = () -> {
                throw cause;
            };

            assertThatThrownBy(() -> apiClientExecutor.execute(integrationName, failingAction))
                .isInstanceOf(ManagerApiException.class)
                .hasMessage("Error in external service: " + integrationName)
                .satisfies(ex -> {
                    final var managerApiException = (ManagerApiException) ex;

                    assertThat(managerApiException.getFieldName())
                        .isEqualTo("integration.error");

                    assertThat(managerApiException.getHttpStatus())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });
        }

        @Test
        @DisplayName(
            "should catch any other Exception and throw ManagerApiException with 500 status"
        )
        void execute_whenActionThrowsUnexpectedException_shouldThrowManagerApiException() {
            final var cause = new RuntimeException("Something went wrong");
            Supplier<Void> failingAction = () -> {
                throw cause;
            };

            assertThatThrownBy(() -> apiClientExecutor.execute(integrationName, failingAction))
                .isInstanceOf(ManagerApiException.class)
                .hasMessage(
                    "Unexpected error while communicating with external service: " + integrationName
                )
                .satisfies(ex -> {
                    final var managerApiException = (ManagerApiException) ex;
                    assertThat(managerApiException.getFieldName())
                        .isEqualTo("integration.unexpected");

                    assertThat(managerApiException.getHttpStatus())
                        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });
        }
    }
}
