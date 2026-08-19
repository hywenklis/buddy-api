package com.buddy.api.domains.account.email.services.impl;

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
            String verificationUrl = emailProperties.templates().verification().url() + token;
            String htmlBody = buildConfirmationEmailBody(verificationUrl);

            log.info("Sending verification email to account={}", accountId);
            managerService.sendEmailNotification(
                List.of(userEmail),
                emailProperties.from(),
                emailProperties.templates().verification().subject(),
                htmlBody
            );
            log.info("Verification email successfully sent to account={}", accountId);
        } catch (com.buddy.api.commons.exceptions.ManagerApiException e) {
            log.error("Failed to send email verification for account={}", accountId, e);
            throw e;
        }
    }

    @Async
    @Override
    public void dispatchPasswordRecoveryEmail(final UUID accountId,
                                              final String userEmail,
                                              final String token
    ) {
        try {
            String recoveryUrl = emailProperties.templates().forgotPassword().url() + token;
            String htmlBody = buildPasswordRecoveryEmailBody(recoveryUrl);

            log.info("Sending password recovery email to account={}", accountId);
            managerService.sendEmailNotification(
                List.of(userEmail),
                emailProperties.from(),
                emailProperties.templates().forgotPassword().subject(),
                htmlBody
            );
            log.info("Password recovery email successfully sent to account={}", accountId);
        } catch (com.buddy.api.commons.exceptions.ManagerApiException e) {
            log.error("Failed to send password recovery email for account={}", accountId, e);
            throw e;
        }
    }

    @Async
    @Override
    public void dispatchPasswordChangedNotification(final UUID accountId, final String userEmail) {
        try {
            String htmlBody = buildPasswordChangedEmailBody();

            log.info("Sending password changed notification email to account={}", accountId);
            managerService.sendEmailNotification(
                List.of(userEmail),
                emailProperties.from(),
                emailProperties.templates().passwordChanged().subject(),
                htmlBody
            );
            log.info("Password changed notification successfully sent to account={}", accountId);
        } catch (com.buddy.api.commons.exceptions.ManagerApiException e) {
            log.error("Failed to send password changed notification for account={}", accountId, e);
            throw e;
        }
    }

    private String buildConfirmationEmailBody(final String verificationUrl) {
        String template =
            emailTemplateLoader.load(emailProperties.templates().verification().templatePath());
        return template.replace("{{url}}", verificationUrl);
    }

    private String buildPasswordRecoveryEmailBody(final String recoveryUrl) {
        String template = emailTemplateLoader.load(
            emailProperties.templates().forgotPassword().templatePath());
        return template.replace("{{url}}", recoveryUrl);
    }

    private String buildPasswordChangedEmailBody() {
        return emailTemplateLoader.load(
            emailProperties.templates().passwordChanged().templatePath());
    }
}
