package com.buddy.api.web.terms.controllers;

import com.buddy.api.domains.terms.services.AcceptTerms;
import com.buddy.api.domains.terms.services.FindTermsVersion;
import com.buddy.api.web.terms.mappers.TermsRequestMapper;
import com.buddy.api.web.terms.mappers.TermsResponseMapper;
import com.buddy.api.web.terms.responses.TermsVersionResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/terms")
@RequiredArgsConstructor
public class TermsController implements TermsControllerDoc {

    private final AcceptTerms acceptTerms;
    private final FindTermsVersion findTermsVersion;
    private final TermsResponseMapper termsResponseMapper;
    private final TermsRequestMapper termsRequestMapper;

    @GetMapping("/active")
    @ResponseStatus(HttpStatus.OK)
    public TermsVersionResponse getActiveTerms() {
        final var termsVersionDto = findTermsVersion.findActive();
        return termsResponseMapper.toTermsVersionResponse(termsVersionDto);
    }

    @PostMapping("/accept")
    @ResponseStatus(HttpStatus.OK)
    public void acceptTermsVersion(@AuthenticationPrincipal final UserDetails userDetails,
                            final HttpServletRequest request
    ) {
        final var acceptTermsDto = termsRequestMapper.toDto(request, userDetails);
        acceptTerms.accept(acceptTermsDto);
    }
}
