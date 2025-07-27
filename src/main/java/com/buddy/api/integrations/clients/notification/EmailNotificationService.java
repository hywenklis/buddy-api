package com.buddy.api.integrations.clients.notification;

import java.util.List;

public interface EmailNotificationService {
    void sendEmail(List<String> recipients, String subject, String htmlBody);
}
