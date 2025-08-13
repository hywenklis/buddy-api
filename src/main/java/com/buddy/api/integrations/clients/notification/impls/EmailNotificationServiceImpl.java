package com.buddy.api.integrations.clients.notification.impls;

import com.buddy.api.commons.configurations.properties.NotificationApiProperties;
import com.buddy.api.integrations.clients.configs.executor.ApiClientExecutor;
import com.buddy.api.integrations.clients.manager.ManagerService;
import com.buddy.api.integrations.clients.notification.EmailNotificationClient;
import com.buddy.api.integrations.clients.notification.EmailNotificationService;
import com.buddy.api.integrations.clients.notification.request.EmailNotificationRequest;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final ManagerService managerService;
    private final EmailNotificationClient notificationClient;
    private final ApiClientExecutor apiClientExecutor;
    private final NotificationApiProperties properties;

    @Override
    public void sendEmail(final List<String> recipients,
                          final String subject,
                          final String htmlBody
    ) {
        log.info("Preparing to send email. Subject: {}", subject);

        final String token = managerService.getValidToken();

        final var request = EmailNotificationRequest.builder()
            .recipients(recipients)
            .emailFrom("buddy.contato.app@gmail.com")
            .subject(subject)
            .text(htmlBody)
            .isHtml(true)
            .attachments(Collections.emptyList())
            .senderEmailNotification("buddy")
            .build();

        final String integrationName = "Notification API - Send Email";
        apiClientExecutor.executeClientCall(integrationName, () -> {
            notificationClient.sendEmail(properties.user(), token, request);
            return null;
        });

        log.info("Email dispatch instruction sent successfully to recipients: {}", recipients);
    }
}