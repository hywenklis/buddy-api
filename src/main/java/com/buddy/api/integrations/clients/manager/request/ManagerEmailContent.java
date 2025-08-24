package com.buddy.api.integrations.clients.manager.request;

import java.util.List;
import lombok.Builder;

@Builder
public record ManagerEmailContent(
    List<String> recipients,
    String emailFrom,
    String subject,
    String text,
    Boolean isHtml,
    List<String> attachments,
    String senderEmailNotification
) { }
