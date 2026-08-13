package com.buddy.api.domains.account.password.services;

import com.buddy.api.web.accounts.requests.ResetPasswordRequest;

public interface ResetPasswordService {
    void resetPassword(ResetPasswordRequest request);
}
