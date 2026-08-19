package com.buddy.api.web.accounts.mappers;

import com.buddy.api.domains.account.password.dtos.ChangePasswordDto;
import com.buddy.api.domains.authentication.dtos.AuthenticatedUser;
import com.buddy.api.web.accounts.requests.ChangePasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.function.Predicate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.core.userdetails.UserDetails;

@Mapper(componentModel = "spring")
public interface ChangePasswordMapperRequest {

    @Mapping(target = "email", source = "userDetails", qualifiedByName = "extractEmail")
    @Mapping(target = "accountId", source = "userDetails", qualifiedByName = "extractAccountId")
    @Mapping(target = "ipAddress", source = "request", qualifiedByName = "extractIp")
    @Mapping(target = "userAgent", source = "request", qualifiedByName = "extractUserAgent")
    @Mapping(target = "currentPassword", source = "body.currentPassword")
    @Mapping(target = "newPassword", source = "body.newPassword")
    ChangePasswordDto toDto(ChangePasswordRequest body,
                            HttpServletRequest request,
                            UserDetails userDetails);

    @Named("extractEmail")
    default String extractEmail(final UserDetails userDetails) {
        return Optional.ofNullable(userDetails)
            .filter(AuthenticatedUser.class::isInstance)
            .map(AuthenticatedUser.class::cast)
            .map(AuthenticatedUser::getEmail)
            .orElseGet(() -> Optional.ofNullable(userDetails)
                .map(UserDetails::getUsername)
                .orElse(null));
    }

    @Named("extractAccountId")
    default java.util.UUID extractAccountId(final UserDetails userDetails) {
        return Optional.ofNullable(userDetails)
            .filter(AuthenticatedUser.class::isInstance)
            .map(AuthenticatedUser.class::cast)
            .map(AuthenticatedUser::getAccountId)
            .orElse(null);
    }

    @Named("extractIp")
    default String extractIp(final HttpServletRequest request) {
        final String unknownIp = "0.0.0.0";

        return Optional.ofNullable(request)
            .map(req -> req.getHeader("X-Forwarded-For"))
            .filter(Predicate.not(String::isBlank))
            .map(header -> header.split(",")[0].trim())

            .or(() -> Optional.ofNullable(request)
                .map(req -> req.getHeader("X-Real-IP"))
                .filter(Predicate.not(String::isBlank)))

            .or(() -> Optional.ofNullable(request)
                .map(HttpServletRequest::getRemoteAddr)
                .filter(Predicate.not(String::isBlank)))

            .orElse(unknownIp);
    }

    @Named("extractUserAgent")
    default String extractUserAgent(final HttpServletRequest request) {
        return Optional.ofNullable(request)
            .map(req -> req.getHeader("User-Agent"))
            .orElse("Unknown");
    }
}
