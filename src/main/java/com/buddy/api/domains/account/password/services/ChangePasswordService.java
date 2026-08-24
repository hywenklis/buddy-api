package com.buddy.api.domains.account.password.services;

import com.buddy.api.domains.account.password.dtos.ChangePasswordDto;

public interface ChangePasswordService {
    void changePassword(ChangePasswordDto changePasswordDto);
}
