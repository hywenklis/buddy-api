package com.buddy.api.web.terms.mappers;

import com.buddy.api.commons.http.ClientIpResolver;
import com.buddy.api.domains.authentication.dtos.AuthenticatedUser;
import com.buddy.api.domains.terms.dtos.AcceptTermsDto;
import com.buddy.api.domains.terms.dtos.CreateTermsVersionDto;
import com.buddy.api.web.terms.requests.CreateTermsVersionRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

@Mapper(componentModel = "spring")
public abstract class TermsRequestMapper {

    @Value("${buddy.security.trusted-proxy-addresses:}")
    private List<String> trustedProxyAddresses = List.of();

    @Mapping(target = "email", source = "userDetails", qualifiedByName = "extractEmail")
    @Mapping(target = "ipAddress", source = "request", qualifiedByName = "extractIp")
    @Mapping(target = "userAgent", source = "request", qualifiedByName = "extractUserAgent")
    public abstract AcceptTermsDto toDto(HttpServletRequest request, UserDetails userDetails);

    @Mapping(
        target = "publishedByAccountEmail",
        source = "userDetails",
        qualifiedByName = "extractEmail"
    )
    @Mapping(target = "versionTag", source = "request.versionTag")
    @Mapping(target = "content", source = "request.content")
    @Mapping(target = "isActive", source = "request.isActive")
    public abstract CreateTermsVersionDto toCreateDto(CreateTermsVersionRequest request,
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

    @Named("extractIp")
    public String extractIp(final HttpServletRequest request) {
        return ClientIpResolver.extract(request, trustedProxyAddresses);
    }

    @Named("extractUserAgent")
    public String extractUserAgent(final HttpServletRequest request) {
        return Optional.ofNullable(request)
            .map(req -> req.getHeader("User-Agent"))
            .orElse("Unknown");
    }
}