package com.buddy.api.web.accounts.controllers;

import com.buddy.api.domains.account.password.services.ResetPasswordService;
import com.buddy.api.web.accounts.mappers.ResetPasswordMapperRequest;
import com.buddy.api.web.accounts.requests.ResetPasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
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
public class ResetPasswordController {

    private final ResetPasswordService resetPasswordService;
    private final ResetPasswordMapperRequest mapper = Mappers.getMapper(
        ResetPasswordMapperRequest.class);

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.OK)
    public void resetPassword(
        @Valid @RequestBody final ResetPasswordRequest request
    ) {
        log.debug("Received reset password request");
        resetPasswordService.resetPassword(mapper.toResetPasswordDto(request));
    }
}
