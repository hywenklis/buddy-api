package com.buddy.api.web.accounts.controllers;

import com.buddy.api.commons.configurations.cache.annotations.RateLimited;
import com.buddy.api.domains.account.email.services.EmailVerificationService;
import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.web.accounts.requests.ConfirmEmailRequest;
import com.buddy.api.web.accounts.requests.RequestEmailVerificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts/verifications")
@RequiredArgsConstructor
public class EmailVerificationController implements EmailVerificationControllerDoc {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @RateLimited(useIp = true,
        operation = "verification",
        emailSpel = "#request.email",
        limitMessage = "Too many verification requests. Please wait a minute before trying again."
    )
    public void requestVerification(
        final @RequestBody @Valid RequestEmailVerificationRequest request) {
        emailVerificationService.requestEmail(new EmailAddress(request.email()));
    }

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.OK)
    @RateLimited(useIp = true,
        operation = "verificationConfirm",
        limitMessage =
            "Too many verification confirmation attempts. Please wait a minute before trying again."
    )
    public void confirm(final @RequestBody @Valid ConfirmEmailRequest request) {
        emailVerificationService.confirmEmail(request.token());
    }
}
