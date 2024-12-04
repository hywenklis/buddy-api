package com.buddy.api.web.defaultresponses;

public record CreatedSuccessResponse(String message) {
    public CreatedSuccessResponse() {
        this("successfully created");
    }
}
