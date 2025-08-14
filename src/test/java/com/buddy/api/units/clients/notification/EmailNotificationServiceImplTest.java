package com.buddy.api.units.clients.notification;

import static com.buddy.api.utils.RandomEmailUtils.generateValidEmailAddress;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.properties.NotificationApiProperties;
import com.buddy.api.integrations.clients.configs.executor.ApiClientExecutor;
import com.buddy.api.integrations.clients.manager.ManagerService;
import com.buddy.api.integrations.clients.notification.EmailNotificationClient;
import com.buddy.api.integrations.clients.notification.impls.EmailNotificationServiceImpl;
import com.buddy.api.integrations.clients.notification.request.EmailNotificationRequest;
import com.buddy.api.units.UnitTestAbstract;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("Unit Tests for EmailNotificationServiceImpl")
class EmailNotificationServiceImplTest extends UnitTestAbstract {

    private static final String INTEGRATION_NAME = "Notification API - Send Email";

    @Mock
    private ManagerService managerService;

    @Mock
    private EmailNotificationClient notificationClient;

    @Mock
    private ApiClientExecutor apiClientExecutor;

    @Mock
    private NotificationApiProperties properties;

    @InjectMocks
    private EmailNotificationServiceImpl emailNotificationService;

    @Test
    @DisplayName("Should build correct request and call notification client on sendEmail")
    void sendEmail_withValidData_shouldCallClientWithCorrectParameters() {
        final var recipients = List.of(
            generateValidEmailAddress().value(),
            generateValidEmailAddress().value()
        );

        final var subject = UUID.randomUUID().toString();
        final var htmlBody = UUID.randomUUID().toString();
        final var apiToken = UUID.randomUUID().toString();
        final var apiUser = UUID.randomUUID().toString();

        when(managerService.getValidToken()).thenReturn(apiToken);
        when(properties.user()).thenReturn(apiUser);

        when(apiClientExecutor.execute(eq(INTEGRATION_NAME), any(Supplier.class)))
            .thenAnswer(invocation -> {
                invocation.<Supplier<Void>>getArgument(1).get();
                return null;
            });

        emailNotificationService.sendEmail(recipients, subject, htmlBody);

        verify(managerService, times(1)).getValidToken();

        final var requestCaptor = ArgumentCaptor.forClass(EmailNotificationRequest.class);
        verify(notificationClient, times(1)).sendEmail(
            eq(apiUser),
            eq(apiToken),
            requestCaptor.capture()
        );

        final var capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.recipients()).isEqualTo(recipients);
        assertThat(capturedRequest.subject()).isEqualTo(subject);
        assertThat(capturedRequest.text()).isEqualTo(htmlBody);
        assertThat(capturedRequest.isHtml()).isTrue();
        assertThat(capturedRequest.emailFrom()).isEqualTo("buddy.contato.app@gmail.com");
        assertThat(capturedRequest.senderEmailNotification()).isEqualTo("buddy");
        assertThat(capturedRequest.attachments().size()).isZero();
    }
}
