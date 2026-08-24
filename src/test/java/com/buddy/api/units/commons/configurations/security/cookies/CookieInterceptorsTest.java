package com.buddy.api.units.commons.configurations.security.cookies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.buddy.api.commons.configurations.security.cookies.ClearCookiesInterceptor;
import com.buddy.api.commons.configurations.security.cookies.CookieInterceptorAdvice;
import com.buddy.api.commons.configurations.security.cookies.CookieManager;
import com.buddy.api.commons.configurations.security.cookies.annotations.ClearCookiesOnSuccess;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.web.authentication.responses.AuthResponse;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

class CookieInterceptorsTest extends UnitTestAbstract {

    @Mock
    private CookieManager cookieManager;

    @InjectMocks
    private ClearCookiesInterceptor clearCookiesInterceptor;

    @InjectMocks
    private CookieInterceptorAdvice cookieInterceptorAdvice;

    @Test
    @DisplayName("Should clear cookies after a successful annotated handler")
    void should_clear_cookies_after_success() {
        HandlerMethod handler = handler("success");
        MockHttpServletResponse response = new MockHttpServletResponse();

        clearCookiesInterceptor.postHandle(new MockHttpServletRequest(), response, handler, null);

        verify(cookieManager).clearCookies(response);
    }

    @Test
    @DisplayName("Should not clear cookies for unsuccessful or unannotated handlers")
    void should_not_clear_cookies_when_not_successful() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(400);
        clearCookiesInterceptor.postHandle(new MockHttpServletRequest(), response,
            handler("success"), null);
        clearCookiesInterceptor.postHandle(new MockHttpServletRequest(), response,
            handler("ordinary"), null);

        MockHttpServletResponse informationalResponse = new MockHttpServletResponse();
        informationalResponse.setStatus(100);
        clearCookiesInterceptor.postHandle(new MockHttpServletRequest(), informationalResponse,
            handler("success"), null);

        verify(cookieManager, never()).clearCookies(response);
        verify(cookieManager, never()).clearCookies(informationalResponse);
    }

    @Test
    @DisplayName("Should ignore handlers that are not controller methods")
    void should_ignore_non_handler_method() {
        clearCookiesInterceptor.postHandle(new MockHttpServletRequest(),
            new MockHttpServletResponse(), new Object(), null);
    }

    @Test
    @DisplayName("Should attach cookies before writing an AuthResponse")
    void should_attach_cookies_before_writing_body() throws Exception {
        Method method = Fixture.class.getDeclaredMethod("response");
        MethodParameter parameter = new MethodParameter(method, -1);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthResponse body = new AuthResponse(java.util.List.of(), ACCESS_TOKEN, REFRESH_TOKEN);

        AuthResponse result = cookieInterceptorAdvice.beforeBodyWrite(body, parameter,
            MediaType.APPLICATION_JSON, StringHttpMessageConverter.class,
            new ServletServerHttpRequest(request), new ServletServerHttpResponse(response));

        assertThat(result).isSameAs(body);
        verify(cookieManager).handleCookies(request, response, ACCESS_TOKEN, REFRESH_TOKEN);
    }

    @Test
    @DisplayName("Should return the response body when HTTP wrappers are not servlet based")
    void should_return_body_for_non_servlet_response() throws Exception {
        Method method = Fixture.class.getDeclaredMethod("response");
        MethodParameter parameter = new MethodParameter(method, -1);
        AuthResponse body = new AuthResponse(java.util.List.of(), ACCESS_TOKEN, REFRESH_TOKEN);

        AuthResponse result = cookieInterceptorAdvice.beforeBodyWrite(body, parameter,
            MediaType.APPLICATION_JSON, StringHttpMessageConverter.class,
            mock(ServerHttpRequest.class), mock(ServerHttpResponse.class));

        assertThat(result).isSameAs(body);

        MockHttpServletRequest request = new MockHttpServletRequest();
        AuthResponse result2 = cookieInterceptorAdvice.beforeBodyWrite(body, parameter,
            MediaType.APPLICATION_JSON, StringHttpMessageConverter.class,
            new ServletServerHttpRequest(request), mock(ServerHttpResponse.class));

        assertThat(result2).isSameAs(body);
    }

    @Test
    @DisplayName("Should report support only for AuthResponse return types")
    void should_report_supported_return_types() throws Exception {
        MethodParameter authParameter =
            new MethodParameter(Fixture.class.getDeclaredMethod("response"), -1);
        MethodParameter otherParameter =
            new MethodParameter(Fixture.class.getDeclaredMethod("ordinary"), -1);

        assertThat(cookieInterceptorAdvice.supports(authParameter,
            StringHttpMessageConverter.class)).isTrue();
        assertThat(cookieInterceptorAdvice.supports(otherParameter,
            StringHttpMessageConverter.class)).isFalse();
    }

    @Test
    @DisplayName("Should return a null body without attaching cookies")
    void should_return_null_body() throws Exception {
        MethodParameter parameter =
            new MethodParameter(Fixture.class.getDeclaredMethod("response"), -1);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(
            cookieInterceptorAdvice.beforeBodyWrite(null, parameter, MediaType.APPLICATION_JSON,
                StringHttpMessageConverter.class, new ServletServerHttpRequest(request),
                new ServletServerHttpResponse(response))).isNull();
    }

    private HandlerMethod handler(final String method) {
        try {
            return new HandlerMethod(new Fixture(), Fixture.class.getDeclaredMethod(method));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    static class Fixture {
        @ClearCookiesOnSuccess
        void success() {
        }

        void ordinary() {
        }

        AuthResponse response() {
            return null;
        }
    }
}
