package com.buddy.api.web.accounts.controllers;

import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.account.services.impl.EmailVerificationServiceImpl;
import com.buddy.api.domains.authentication.dtos.AuthenticatedUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

    private final EmailVerificationServiceImpl emailVerificationService;
    private final FindAccount accountService;

    @PostMapping("/request")
    public ResponseEntity<Void> requestVerification(final @AuthenticationPrincipal AuthenticatedUser user) {
        AccountDto account = accountService.findByEmail(user.getEmail());
        emailVerificationService.requestVerificationEmail(account);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(final @RequestBody ConfirmEmailRequest request) {
        emailVerificationService.confirmEmail(request.getToken());
        return ResponseEntity.noContent().build();
    }

    @Setter
    @Getter
    public static class ConfirmEmailRequest {
        private String token;
    }
}

