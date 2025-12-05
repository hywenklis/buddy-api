package com.buddy.api.web.accounts.controllers;

import com.buddy.api.domains.account.services.CreateAccount;
import com.buddy.api.web.accounts.mappers.AccountMapperRequest;
import com.buddy.api.web.accounts.requests.AccountRequest;
import com.buddy.api.web.defaultresponses.CreatedSuccessResponse;
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
public class CreateAccountController implements CreateAccountControllerDoc {
    private final CreateAccount createAccount;
    private final AccountMapperRequest mapperRequest;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedSuccessResponse registration(
        @Valid @RequestBody final AccountRequest accountRequest) {
        createAccount.create(mapperRequest.toAccountDto(accountRequest));
        return new CreatedSuccessResponse();
    }
}
