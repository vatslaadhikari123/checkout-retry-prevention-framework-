package com.vatsla.retry.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks individual processing methods for attempt tracking.
 * Interceptor tracks invocation status and records attempt counters post-execution.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UpdateBusObjRetryAttempts {
    /** Positional index of the retry-aware context argument */
    int retryJobContextMethodArgIndex() default 0;

    /** Positional index of the entity/DTO argument */
    int retryResourceMethodArgIndex() default 1;
}
