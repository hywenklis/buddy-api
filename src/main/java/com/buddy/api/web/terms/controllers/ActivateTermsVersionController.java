package com.buddy.api.web.terms.controllers;

import com.buddy.api.domains.terms.services.ActivateTermsVersion;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/terms")
@RequiredArgsConstructor
public class ActivateTermsVersionController implements ActivateTermsVersionControllerDoc {
    private final ActivateTermsVersion activateTermsVersion;

    @Override
    @PatchMapping("/{termsVersionId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void activateVersion(@PathVariable final UUID termsVersionId) {
        activateTermsVersion.activate(termsVersionId);
    }
}
