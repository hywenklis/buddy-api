package com.buddy.api.web.accounts.controllers;

import com.buddy.api.web.accounts.requests.AccountRequest;
import com.buddy.api.web.accounts.responses.AccountResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts/register")
public class CreateAccountController {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse registration(@Valid @RequestBody final AccountRequest accountRequest) {
        return new AccountResponse("successfully created");
    }
}
