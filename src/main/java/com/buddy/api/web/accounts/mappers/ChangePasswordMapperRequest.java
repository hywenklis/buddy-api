package com.buddy.api.web.accounts.mappers;

import com.buddy.api.commons.http.HttpRequestExtractor;
import com.buddy.api.domains.account.password.dtos.ChangePasswordDto;
import com.buddy.api.domains.authentication.dtos.AuthenticatedUser;
import com.buddy.api.web.accounts.requests.ChangePasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

@Mapper(componentModel = "spring")
public abstract class ChangePasswordMapperRequest {

    @Autowired
    protected HttpRequestExtractor httpRequestExtractor;

    @Mapping(target = "email", source = "userDetails", qualifiedByName = "extractEmail")
    @Mapping(target = "accountId", source = "userDetails", qualifiedByName = "extractAccountId")
    @Mapping(target = "ipAddress", source = "request", qualifiedByName = "extractIp")
    @Mapping(target = "userAgent", source = "request", qualifiedByName = "extractUserAgent")
    @Mapping(target = "currentPassword", source = "body.currentPassword")
    @Mapping(target = "newPassword", source = "body.newPassword")
    public abstract ChangePasswordDto toDto(ChangePasswordRequest body,
                                            HttpServletRequest request,
                                            UserDetails userDetails);

    @Named("extractEmail")
    public String extractEmail(final UserDetails userDetails) {
        return Optional.ofNullable(userDetails)
            .filter(AuthenticatedUser.class::isInstance)
            .map(AuthenticatedUser.class::cast)
            .map(AuthenticatedUser::getEmail)
            .orElseGet(() -> Optional.ofNullable(userDetails)
                .map(UserDetails::getUsername)
                .orElse(null));
    }

    @Named("extractAccountId")
    public UUID extractAccountId(final UserDetails userDetails) {
        return Optional.ofNullable(userDetails)
            .filter(AuthenticatedUser.class::isInstance)
            .map(AuthenticatedUser.class::cast)
            .map(AuthenticatedUser::getAccountId)
            .orElse(null);
    }

    @Named("extractIp")
    public String extractIp(final HttpServletRequest request) {
        return httpRequestExtractor.extractIp(request);
    }

    @Named("extractUserAgent")
    public String extractUserAgent(final HttpServletRequest request) {
        return httpRequestExtractor.extractUserAgent(request);
    }
}
