package com.buddy.api.web.accounts.controllers;

import com.buddy.api.domains.account.services.CreateAccount;
import com.buddy.api.web.accounts.mappers.AccountMapperRequest;
import com.buddy.api.web.accounts.requests.AccountRequest;
import com.buddy.api.web.defaultresponses.CreatedSuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Endpoint related to account registration")
public class CreateAccountController {
    private final CreateAccount createAccount;
    private final AccountMapperRequest mapperRequest;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Register account",
        description = "Register account with their appropriate information"
    )
    public CreatedSuccessResponse registration(
        @Valid @RequestBody final AccountRequest accountRequest) {
        createAccount.create(mapperRequest.toAccountDto(accountRequest));
        return new CreatedSuccessResponse();
    }
}
