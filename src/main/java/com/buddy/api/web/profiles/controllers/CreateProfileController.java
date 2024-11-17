package com.buddy.api.web.profiles.controllers;

import com.buddy.api.web.profiles.requests.ProfileRequest;
import com.buddy.api.web.profiles.responses.ProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/profiles/register")
public class CreateProfileController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse registration(@Valid @RequestBody final ProfileRequest request) {
        return new ProfileResponse("Successfully created");
    }
}
