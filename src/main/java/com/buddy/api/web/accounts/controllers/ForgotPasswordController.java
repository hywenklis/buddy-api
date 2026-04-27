package com.buddy.api.web.accounts.controllers;

import com.buddy.api.commons.exceptions.AccountUnavailableException;
import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.email.services.ForgotPasswordService;
import com.buddy.api.domains.account.services.FindAccount;
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
    private final FindAccount findAccount;

    @PostMapping("/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AcceptedSuccessResponse forgotPassword(
        @Valid @RequestBody final ForgotPasswordRequest request
    ) {

        String email = request.email();
        log.debug("Received password recovery request");

        try {
            AccountDto account = findAccount.findByEmail(email);
            forgotPasswordService.requestPasswordRecovery(account);

        } catch (NotFoundException | AccountUnavailableException e) {
            log.debug(
                "Password recovery processed (email existence protected):",
                e);
        }

        return new AcceptedSuccessResponse();
    }
}
