package com.buddy.api.integrations.clients.notification.request;

import java.util.List;
import lombok.Builder;

@Builder
public record EmailNotificationRequest(
    List<String> recipients,
    String emailFrom,
    String subject,
    String text,
    Boolean isHtml,
    List<String> attachments,
    String senderEmailNotification
) {}
