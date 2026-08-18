package com.buddy.api.web.accounts.controllers;

import com.buddy.api.domains.account.password.services.ChangePasswordService;
import com.buddy.api.web.accounts.mappers.ChangePasswordMapperRequest;
import com.buddy.api.web.accounts.requests.ChangePasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts/password")
@RequiredArgsConstructor
@Slf4j
public class ChangePasswordController implements ChangePasswordControllerDoc {

    private final ChangePasswordService changePasswordService;
    private final ChangePasswordMapperRequest mapper;

    @Override
    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(
        @Valid @RequestBody final ChangePasswordRequest request,
        final HttpServletRequest httpRequest,
        @AuthenticationPrincipal final UserDetails userDetails
    ) {
        log.debug("Received change password request for user: {}", userDetails.getUsername());
        final var dto = mapper.toDto(request, httpRequest, userDetails);
        changePasswordService.changePassword(dto);
    }
}
