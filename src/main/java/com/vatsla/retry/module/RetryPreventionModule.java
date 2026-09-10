package com.vatsla.retry.module;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.vatsla.retry.annotation.FilterBusObjWithMaxAttempts;
import com.vatsla.retry.annotation.UpdateBusObjRetryAttempts;
import com.vatsla.retry.aspect.BusObjProactivePreventiveRetryAspect;
import com.vatsla.retry.aspect.BusObjUpdateRetryAttemptsAspect;

public class RetryPreventionModule extends AbstractModule {

    @Override
    protected void configure() {
        // Phase 1 Aspect Registration
        BusObjProactivePreventiveRetryAspect proactiveAspect = new BusObjProactivePreventiveRetryAspect();
        requestInjection(proactiveAspect);
        bindInterceptor(
                Matchers.any(),
                Matchers.annotatedWith(FilterBusObjWithMaxAttempts.class),
                proactiveAspect
        );

        // Phase 2 Aspect Registration
        BusObjUpdateRetryAttemptsAspect updateAspect = new BusObjUpdateRetryAttemptsAspect();
        requestInjection(updateAspect);
        bindInterceptor(
                Matchers.any(),
                Matchers.annotatedWith(UpdateBusObjRetryAttempts.class),
                updateAspect
        );
    }
}
