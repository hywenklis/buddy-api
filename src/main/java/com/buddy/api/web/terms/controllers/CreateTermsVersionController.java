package com.buddy.api.web.terms.controllers;

import com.buddy.api.domains.terms.services.CreateTermsVersion;
import com.buddy.api.web.terms.mappers.TermsRequestMapper;
import com.buddy.api.web.terms.mappers.TermsResponseMapper;
import com.buddy.api.web.terms.requests.CreateTermsVersionRequest;
import com.buddy.api.web.terms.responses.TermsVersionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/terms")
@RequiredArgsConstructor
public class CreateTermsVersionController implements CreateTermsVersionControllerDoc {

    private final CreateTermsVersion createTermsVersion;
    private final TermsRequestMapper termsRequestMapper;
    private final TermsResponseMapper termsResponseMapper;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TermsVersionResponse createVersion(
        @AuthenticationPrincipal final UserDetails userDetails,
        @Valid @RequestBody final CreateTermsVersionRequest request
    ) {
        final var dto = termsRequestMapper.toCreateDto(request, userDetails);
        final var result = createTermsVersion.create(dto);
        return termsResponseMapper.toTermsVersionResponse(result);
    }
}
