package com.buddy.api.web.accounts.controllers;

import com.buddy.api.commons.configurations.cache.annotations.RateLimited;
import com.buddy.api.domains.account.email.services.ForgotPasswordService;
import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.web.accounts.requests.ForgotPasswordRequest;
import com.buddy.api.web.defaultresponses.AcceptedSuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts/password")
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @PostMapping("/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @RateLimited(
        operation = "password-recovery",
        emailSpel = "#request.email",
        limitMessage = "Too many password recovery requests. "
            + "Please wait a minute before trying again."
    )
    public AcceptedSuccessResponse forgotPassword(
        @Valid @RequestBody final ForgotPasswordRequest request
    ) {
        log.debug("Received password recovery request");
        forgotPasswordService.requestPasswordRecovery(new EmailAddress(request.email()));
        return new AcceptedSuccessResponse();
    }
}
