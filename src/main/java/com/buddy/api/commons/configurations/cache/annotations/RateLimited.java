package com.buddy.api.commons.configurations.cache.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    String operation();
    String emailSpel() default "";
    boolean useIp() default true;
    String limitMessage() default "Too many requests. Please wait before trying again.";
}
