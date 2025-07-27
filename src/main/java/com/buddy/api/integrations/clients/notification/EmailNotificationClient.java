package com.buddy.api.integrations.clients.notification;

import com.buddy.api.integrations.clients.configs.FeignConfig;
import com.buddy.api.integrations.clients.notification.request.EmailNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "notification-api",
    url = "${notification.api.base-url}",
    configuration = FeignConfig.class
)
public interface EmailNotificationClient {

    @PostMapping("/v1/emails")
    void sendEmail(
        @RequestHeader("X-User") String user,
        @RequestHeader("X-Token") String token,
        @RequestBody EmailNotificationRequest request
    );
}
