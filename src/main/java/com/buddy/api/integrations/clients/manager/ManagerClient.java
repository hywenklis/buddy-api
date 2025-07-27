package com.buddy.api.integrations.clients.manager;

import com.buddy.api.integrations.clients.configs.FeignConfig;
import com.buddy.api.integrations.clients.manager.request.ManagerAuthRequest;
import com.buddy.api.integrations.clients.manager.response.ManagerAuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "managerClient",
    url = "${manager.api.base-url}",
    configuration = FeignConfig.class
)
public interface ManagerClient {
    @PostMapping("/v1/auth/login")
    ManagerAuthResponse login(
        @RequestBody ManagerAuthRequest request,
        @RequestHeader("User-Agent") String userAgent,
        @RequestHeader("Ip-Address") String ipAddress
    );
}
