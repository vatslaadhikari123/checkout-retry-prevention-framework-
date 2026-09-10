package com.vatsla.retry.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks batch methods for proactive filtering.
 * Interceptor extracts SearchCriteria from arguments and appends exclusion filters
 * for entities that have already exceeded the maximum retry limit.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterBusObjWithMaxAttempts {
    /** Positional index of the retry-aware context argument */
    int retryJobContextMethodArgIndex() default 0;

    /** Positional index of the SearchCriteria argument */
    int searchCriteriaMethodArgIndex() default 1;
}
