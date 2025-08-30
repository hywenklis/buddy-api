package com.buddy.api.domains.account.email.services.impls;

import com.buddy.api.commons.configurations.properties.EmailProperties;
import com.buddy.api.domains.account.email.services.EmailSender;
import com.buddy.api.domains.account.email.services.EmailTemplateLoaderService;
import com.buddy.api.integrations.clients.manager.ManagerService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailSenderImpl implements EmailSender {
    private final ManagerService managerService;
    private final EmailProperties emailProperties;
    private final EmailTemplateLoaderService emailTemplateLoader;

    @Async
    @Override
    public void dispatchVerificationEmail(final UUID accountId,
                                          final String userEmail,
                                          final String token
    ) {
        try {
            String verificationUrl = emailProperties.templates().url() + token;
            String htmlBody = buildConfirmationEmailBody(verificationUrl);

            log.info("Sending verification email to account={}", accountId);
            managerService.sendEmailNotification(
                List.of(userEmail),
                emailProperties.templates().from(),
                emailProperties.templates().subject(),
                htmlBody
            );
            log.info("Verification email successfully sent to account={}", accountId);
        } catch (Exception e) {
            log.error("Failed to send email verification for account={}", accountId, e);
            throw e;
        }
    }

    private String buildConfirmationEmailBody(final String verificationUrl) {
        String template = emailTemplateLoader.load(emailProperties.templates().templatePath());
        return template.replace("{{url}}", verificationUrl);
    }
}
