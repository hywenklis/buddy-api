package com.buddy.api.web.defaultresponses;

public record AcceptedSuccessResponse(String message) {
    public AcceptedSuccessResponse() {
        this("request accepted");
    }
}
