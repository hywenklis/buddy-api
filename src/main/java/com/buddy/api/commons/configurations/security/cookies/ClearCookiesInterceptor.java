package com.buddy.api.commons.configurations.security.cookies;

import com.buddy.api.commons.configurations.security.cookies.annotations.ClearCookiesOnSuccess;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClearCookiesInterceptor implements HandlerInterceptor {

    private final CookieManager cookieManager;

    @Override
    public void postHandle(@NonNull final HttpServletRequest request,
                           @NonNull final HttpServletResponse response,
                           @NonNull final Object handler,
                           final ModelAndView modelAndView
    ) {
        if (handler instanceof HandlerMethod handlerMethod) {
            if (handlerMethod.hasMethodAnnotation(ClearCookiesOnSuccess.class)) {
                int status = response.getStatus();
                if (status >= 200 && status < 300) {
                    log.debug("Intercepting successful request to clear cookies");
                    cookieManager.clearCookies(response);
                }
            }
        }
    }
}
