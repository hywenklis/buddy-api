package com.buddy.api.units;

import static com.buddy.api.domains.profile.enums.ProfileTypeEnum.ADMIN;
import static com.buddy.api.domains.profile.enums.ProfileTypeEnum.SHELTER;
import static com.buddy.api.domains.profile.enums.ProfileTypeEnum.USER;

import com.buddy.api.utils.RandomEmailUtils;
import java.util.List;
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
    protected static final String VALID_JWT = "valid-jwt-token";
    protected static final String EMAIL_VALUE = RandomEmailUtils.generateValidEmail();
    protected static final String BEARER_PREFIX = "Bearer ";
    protected static final String BEARER_TOKEN = BEARER_PREFIX + VALID_JWT;
    protected static final String SECRET_KEY = "uma-chave-secreta-muito-longa-e-segura-com-mais-de-32-bytes";
    protected static final long ACCESS_TOKEN_EXPIRATION = 3600000; // 1 hora em ms
    protected static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 dias em ms
    protected static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    protected static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    protected static final String AUTHORIZATION_HEADER = "Authorization";
    protected static final List<String> PROFILES = List.of(
        USER.name(),
        ADMIN.name(),
        SHELTER.name()
    );
    protected static final String ORIGIN_NAME = "Origin";
}
