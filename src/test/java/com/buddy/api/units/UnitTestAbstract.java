package com.buddy.api.units;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class UnitTestAbstract {
    protected static final int DEFAULT_PAGE_NUMBER = 0;
    protected static final int DEFAULT_PAGE_SIZE = 20;
    protected static final String ACCESS_TOKEN = "access-token-sample";
    protected static final String REFRESH_TOKEN = "refresh-token-sample";
    protected static final String ACCESS_TOKEN_NAME = "access_token";
    protected static final String REFRESH_TOKEN_NAME = "refresh_token";
    protected static final String COOKIE_ATTRIBUTES = "Path=/; HttpOnly; Secure; SameSite=Strict";
    protected static final String SET_COOKIE_HEADER = "Set-Cookie";
    protected static final int ONE_HOUR_IN_SECONDS = 3600; // 1 hora em segundos
    protected static final int SEVEN_DAYS_IN_SECONDS = 604800; // 7 dias em segundos
}
