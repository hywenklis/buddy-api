package com.buddy.api.web.accounts.controllers;

import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.EmailVerificationService;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.authentication.dtos.AuthenticatedUser;
import com.buddy.api.web.accounts.requests.ConfirmEmailRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts/verifications")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;
    private final FindAccount accountService;

    @PostMapping("/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestVerification(
        final @AuthenticationPrincipal AuthenticatedUser user
    ) {
        AccountDto account = accountService.findByEmail(user.getEmail());
        emailVerificationService.requestEmail(account);
    }

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.OK)
    public void confirm(
        final @RequestBody @Valid ConfirmEmailRequest request,
        final @AuthenticationPrincipal AuthenticatedUser user
    ) {
        AccountDto account = accountService.findByEmail(user.getEmail());
        emailVerificationService.confirmEmail(request.token(), account);
    }
}
