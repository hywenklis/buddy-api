package com.buddy.api.web.accounts.mappers;

import com.buddy.api.domains.account.password.dtos.ResetPasswordDto;
import com.buddy.api.web.accounts.requests.ResetPasswordRequest;
import org.mapstruct.Mapper;

@Mapper
public interface ResetPasswordMapperRequest {
    ResetPasswordDto toResetPasswordDto(ResetPasswordRequest request);
}
