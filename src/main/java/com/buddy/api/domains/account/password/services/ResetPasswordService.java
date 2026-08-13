package com.buddy.api.domains.account.password.services;

import com.buddy.api.domains.account.password.dtos.ResetPasswordDto;

public interface ResetPasswordService {
    void resetPassword(ResetPasswordDto request);
}
