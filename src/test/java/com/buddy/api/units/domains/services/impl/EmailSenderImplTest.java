package com.buddy.api.units.domains.services.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.properties.EmailProperties;
import com.buddy.api.commons.configurations.properties.EmailProperties.Template;
import com.buddy.api.commons.configurations.properties.EmailProperties.TemplateWithUrl;
import com.buddy.api.commons.configurations.properties.EmailProperties.Templates;
import com.buddy.api.commons.exceptions.ManagerApiException;
import com.buddy.api.domains.account.email.services.EmailTemplateLoaderService;
import com.buddy.api.domains.account.email.services.impl.EmailSenderImpl;
import com.buddy.api.integrations.clients.manager.ManagerService;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;

class EmailSenderImplTest extends UnitTestAbstract {
    private static final String EMAIL_SERVICE_FAILURE = "Email service failure";

    @Mock
    private ManagerService managerService;

    @Mock
    private EmailProperties emailProperties;

    @Mock
    private Templates templates;

    @Mock
    private TemplateWithUrl verificationTemplate;

    @Mock
    private TemplateWithUrl forgotPasswordTemplate;

    @Mock
    private Template passwordChangedTemplate;

    @Mock
    private EmailTemplateLoaderService emailTemplateLoader;

    @InjectMocks
    private EmailSenderImpl emailSender;

    private UUID accountId;
    private String userEmail;
    private String token;
    private String verificationUrl;
    private String templatePath;
    private String from;
    private String subject;
    private String template;
    private String forgotPasswordUrl;
    private String forgotPasswordTemplatePath;
    private String forgotPasswordSubject;
    private String forgotPasswordTemplateBody;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        userEmail = RandomEmailUtils.generateValidEmail();
        token = UUID.randomUUID().toString();
        verificationUrl = "http://example.com/verify?token=" + token;
        templatePath = "/templates/email-verification.html";
        from = "no-reply@buddy.com";
        subject = "Verify Your Email";
        template = "<html><a href='{{url}}'>Verify</a></html>";
        forgotPasswordUrl = "http://example.com/recover?token=" + token;
        forgotPasswordTemplatePath = "/templates/forgot-password.html";
        forgotPasswordSubject = "Recover Your Password";
        forgotPasswordTemplateBody = "<html><a href='{{url}}'>Recover</a></html>";

        when(emailProperties.templates()).thenReturn(templates);
        when(emailProperties.from()).thenReturn(from);
    }

    @Nested
    @DisplayName("Tests for dispatchVerificationEmail method")
    class DispatchVerificationEmailTests {

        @BeforeEach
        void setupVerificationStubs() {
            when(templates.verification()).thenReturn(verificationTemplate);
            when(verificationTemplate.url()).thenReturn("http://example.com/verify?token=");
            when(verificationTemplate.templatePath()).thenReturn(templatePath);
            when(verificationTemplate.subject()).thenReturn(subject);
            when(emailTemplateLoader.load(templatePath)).thenReturn(template);
        }

        @Test
        @DisplayName("Should send verification email successfully")
        void should_send_verification_email_successfully() {
            emailSender.dispatchVerificationEmail(accountId, userEmail, token);

            verify(managerService, times(1)).sendEmailNotification(
                List.of(userEmail),
                from,
                subject,
                template.replace("{{url}}", verificationUrl)
            );
        }

        @Test
        @DisplayName("Should throw exception and log error when email sending fails")
        void should_throw_exception_and_log_error_when_email_sending_fails() {
            var exception = new com.buddy.api.commons.exceptions.ManagerApiException(
                EMAIL_SERVICE_FAILURE, "email",
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                new RuntimeException());
            doThrow(exception).when(managerService)
                .sendEmailNotification(anyList(), anyString(), anyString(), anyString());

            assertThatThrownBy(
                () -> emailSender.dispatchVerificationEmail(accountId, userEmail, token))
                .isInstanceOf(com.buddy.api.commons.exceptions.ManagerApiException.class)
                .hasMessage(EMAIL_SERVICE_FAILURE);
        }
    }

    @Nested
    @DisplayName("Tests for dispatchPasswordRecoveryEmail method")
    class DispatchPasswordRecoveryEmailTests {

        @BeforeEach
        void setupForgotPasswordStubs() {
            when(templates.forgotPassword()).thenReturn(forgotPasswordTemplate);
            when(forgotPasswordTemplate.url()).thenReturn("http://example.com/recover?token=");
            when(forgotPasswordTemplate.templatePath()).thenReturn(forgotPasswordTemplatePath);
            when(forgotPasswordTemplate.subject()).thenReturn(forgotPasswordSubject);
            when(emailTemplateLoader.load(forgotPasswordTemplatePath)).thenReturn(
                forgotPasswordTemplateBody);
        }

        @Test
        @DisplayName("Should send password recovery email successfully")
        void should_send_password_recovery_email_successfully() {
            emailSender.dispatchPasswordRecoveryEmail(accountId, userEmail, token);

            verify(managerService, times(1)).sendEmailNotification(
                List.of(userEmail),
                from,
                forgotPasswordSubject,
                forgotPasswordTemplateBody.replace("{{url}}", forgotPasswordUrl)
            );
        }

        @Test
        @DisplayName("Should throw exception and log error when password recovery fails")
        void should_throw_exception_and_log_error_when_password_recovery_fails() {
            var exception = new com.buddy.api.commons.exceptions.ManagerApiException(
                EMAIL_SERVICE_FAILURE, "email",
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                new RuntimeException());
            doThrow(exception).when(managerService)
                .sendEmailNotification(anyList(), anyString(), anyString(), anyString());

            assertThatThrownBy(
                () -> emailSender.dispatchPasswordRecoveryEmail(accountId, userEmail, token))
                .isInstanceOf(com.buddy.api.commons.exceptions.ManagerApiException.class)
                .hasMessage(EMAIL_SERVICE_FAILURE);
        }
    }

    @Nested
    @DisplayName("Tests for dispatchPasswordChangedNotification method")
    class DispatchPasswordChangedNotificationTests {

        @BeforeEach
        void setupPasswordChangedStubs() {
            when(templates.passwordChanged()).thenReturn(passwordChangedTemplate);
            when(passwordChangedTemplate.templatePath())
                .thenReturn("/templates/password-changed.html");
            when(passwordChangedTemplate.subject()).thenReturn("Password changed");
            when(emailTemplateLoader.load("/templates/password-changed.html"))
                .thenReturn("<html>Password changed</html>");
        }

        @Test
        @DisplayName("Should send password changed notification successfully")
        void should_send_password_changed_notification_successfully() {
            emailSender.dispatchPasswordChangedNotification(accountId, userEmail);

            verify(managerService).sendEmailNotification(
                List.of(userEmail), from, "Password changed", "<html>Password changed</html>");
        }

        @Test
        @DisplayName("Should throw exception when password changed notification fails")
        void should_throw_exception_when_password_changed_notification_fails() {
            final var exception = new ManagerApiException(
                EMAIL_SERVICE_FAILURE, "email",
                HttpStatus.INTERNAL_SERVER_ERROR,
                new RuntimeException()
            );
            doThrow(exception).when(managerService)
                .sendEmailNotification(anyList(), anyString(), anyString(), anyString());

            assertThatThrownBy(
                () -> emailSender.dispatchPasswordChangedNotification(accountId, userEmail))
                .isInstanceOf(ManagerApiException.class)
                .hasMessage(EMAIL_SERVICE_FAILURE);
        }
    }
}
