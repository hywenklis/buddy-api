package com.buddy.api.web.terms.mappers;

import com.buddy.api.commons.http.HttpRequestExtractor;
import com.buddy.api.domains.authentication.dtos.AuthenticatedUser;
import com.buddy.api.domains.terms.dtos.AcceptTermsDto;
import com.buddy.api.domains.terms.dtos.CreateTermsVersionDto;
import com.buddy.api.web.terms.requests.CreateTermsVersionRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.core.userdetails.UserDetails;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = {HttpRequestExtractor.class}
)
public interface TermsRequestMapper {

    @Mapping(target = "email", source = "userDetails", qualifiedByName = "extractEmail")
    @Mapping(target = "ipAddress", source = "request", qualifiedByName = "extractIp")
    @Mapping(target = "userAgent", source = "request", qualifiedByName = "extractUserAgent")
    AcceptTermsDto toDto(HttpServletRequest request, UserDetails userDetails);

    @Mapping(
        target = "publishedByAccountEmail",
        source = "userDetails",
        qualifiedByName = "extractEmail"
    )
    @Mapping(target = "versionTag", source = "request.versionTag")
    @Mapping(target = "content", source = "request.content")
    @Mapping(target = "isActive", source = "request.isActive")
    CreateTermsVersionDto toCreateDto(CreateTermsVersionRequest request, UserDetails userDetails);

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
}