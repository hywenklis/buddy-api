package com.buddy.api.units.domains.services.impls;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.properties.EmailProperties;
import com.buddy.api.domains.account.email.services.EmailTemplateLoaderService;
import com.buddy.api.domains.account.email.services.impls.EmailSenderImpl;
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

class EmailSenderImplTest extends UnitTestAbstract {

    @Mock
    private ManagerService managerService;

    @Mock
    private EmailProperties emailProperties;

    @Mock
    private EmailProperties.Templates templates;

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

        when(emailProperties.templates()).thenReturn(templates);
        when(templates.url()).thenReturn("http://example.com/verify?token=");
        when(templates.templatePath()).thenReturn(templatePath);
        when(templates.from()).thenReturn(from);
        when(templates.subject()).thenReturn(subject);
        when(emailTemplateLoader.load(templatePath)).thenReturn(template);
    }

    @Nested
    @DisplayName("Tests for dispatchVerificationEmail method")
    class DispatchVerificationEmailTests {

        @Test
        @DisplayName("Should send verification email successfully")
        void should_send_verification_email_successfully() {
            emailSender.dispatchVerificationEmail(accountId, userEmail, token);

            verify(emailProperties, times(4)).templates();
            verify(templates, times(1)).url();
            verify(templates, times(1)).from();
            verify(templates, times(1)).subject();
            verify(emailTemplateLoader, times(1)).load(templatePath);
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
            RuntimeException exception = new RuntimeException("Email service failure");
            doThrow(exception).when(managerService)
                .sendEmailNotification(anyList(), anyString(), anyString(), anyString());

            assertThatThrownBy(
                () -> emailSender.dispatchVerificationEmail(accountId, userEmail, token))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email service failure");

            verify(emailProperties, times(4)).templates();
            verify(templates, times(1)).url();
            verify(templates, times(1)).from();
            verify(templates, times(1)).subject();
            verify(emailTemplateLoader, times(1)).load(templatePath);
            verify(managerService, times(1)).sendEmailNotification(
                List.of(userEmail),
                from,
                subject,
                template.replace("{{url}}", verificationUrl)
            );
        }
    }
}
