package com.buddy.api.web.accounts.controllers;

import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.services.EmailVerificationService;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.authentication.dtos.AuthenticatedUser;
import com.buddy.api.web.accounts.requests.ConfirmEmailRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts/verifications")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;
    private final FindAccount accountService;

    @PostMapping("/request")
    public ResponseEntity<Void> requestVerification(
        final @AuthenticationPrincipal AuthenticatedUser user
    ) {
        AccountDto account = accountService.findByEmail(user.getEmail());
        emailVerificationService.requestEmail(account);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(final @RequestBody @Valid ConfirmEmailRequest request) {
        emailVerificationService.confirmEmail(request.token());
        return ResponseEntity.ok().build();
    }
}
