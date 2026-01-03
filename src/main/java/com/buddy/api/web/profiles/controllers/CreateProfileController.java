package com.buddy.api.web.profiles.controllers;

import com.buddy.api.domains.profile.services.CreateProfile;
import com.buddy.api.web.defaultresponses.CreatedSuccessResponse;
import com.buddy.api.web.profiles.mappers.ProfileMapperRequest;
import com.buddy.api.web.profiles.requests.ProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/profiles")
@RequiredArgsConstructor
public class CreateProfileController implements CreateProfileControllerDoc {

    private final CreateProfile createProfile;
    private final ProfileMapperRequest mapperRequest;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCOPE_VERIFIED')")
    public CreatedSuccessResponse registration(@Valid @RequestBody final ProfileRequest request) {
        createProfile.create(mapperRequest.toProfileDto(request));
        return new CreatedSuccessResponse();
    }
}
