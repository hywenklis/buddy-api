package com.buddy.api.commons.configurations.security.cookies;

import com.buddy.api.web.authentication.responses.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class CookieInterceptorAdvice implements ResponseBodyAdvice<AuthResponse> {

    private final CookieManager cookieManager;

    @Override
    public boolean supports(final MethodParameter returnType,
                            @NonNull final Class<? extends HttpMessageConverter<?>> converterType) {
        return AuthResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public AuthResponse beforeBodyWrite(final AuthResponse body,
                                        @NonNull final MethodParameter returnType,
                                        @NonNull final MediaType selectedContentType,
                                        @NonNull final Class<? extends HttpMessageConverter<?>> 
                                                selectedConverterType,
                                        @NonNull final ServerHttpRequest request,
                                        @NonNull final ServerHttpResponse response
    ) {
        if (request instanceof ServletServerHttpRequest servletRequest
            && response instanceof ServletServerHttpResponse servletResponse
            && body != null) {
            log.debug("Intercepting AuthResponse to attach cookies if necessary");
            cookieManager.handleCookies(
                servletRequest.getServletRequest(),
                servletResponse.getServletResponse(),
                body.accessToken(),
                body.refreshToken()
            );
        }
        return body;
    }
}
