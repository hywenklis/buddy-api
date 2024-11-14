package com.buddy.api.web.accounts.requests;

public record AccountRequest(
        String email,
        String phoneNumber,
        String password,
        Boolean termsOfUserConsent
) {
}
