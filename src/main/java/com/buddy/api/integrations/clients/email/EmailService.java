package com.buddy.api.integrations.clients.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final EmailClient emailClient;

    @Value("${email.api.base-url}")
    private String baseUrl;

    public EmailService(final EmailClient emailClient) {
        this.emailClient = emailClient;
    }

    public void sendVerificationEmail(final String to, final String token) {
        String url = baseUrl + "/verify?token=" + token;
        String body = "Clique no link para verificar seu e-mail: " + url;

        EmailClient.EmailRequest request = new EmailClient.EmailRequest();
        request.setTo(to);
        request.setSubject("Verificação de E-mail Buddy");
        request.setBody(body);

        emailClient.sendEmail(request);
    }
}

