package com.buddy.api.integrations.clients.email;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "emailClient", url = "${email.api.base-url}")
public interface EmailClient {

    @PostMapping("/api/emails/send")
    void sendEmail(@RequestBody EmailRequest request);

    @Data
    class EmailRequest {
        private String to;
        private String subject;
        private String body;
    }
}

