package com.buddy.api.web.profiles.controllers;

import com.buddy.api.domains.profile.services.CreateProfile;
import com.buddy.api.web.defaultresponses.CreatedSuccessResponse;
import com.buddy.api.web.profiles.mappers.ProfileMapperRequest;
import com.buddy.api.web.profiles.requests.ProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/profiles/register")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Endpoint related to profile registration")
public class CreateProfileController {

    private final CreateProfile createProfile;
    private final ProfileMapperRequest mapperRequest;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Register profile",
        description = "Register profile with their appropriate information"
    )
    public CreatedSuccessResponse registration(@Valid @RequestBody final ProfileRequest request) {
        createProfile.create(mapperRequest.toProfileDto(request));
        return new CreatedSuccessResponse();
    }
}
