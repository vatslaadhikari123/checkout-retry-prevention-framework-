package com.alnt.jobserver.retry.prevention.module;

import com.alnt.jobserver.platform.base.retry.busobj.annotation.CheckBusObjWithMaxAttempts;
import com.alnt.jobserver.platform.base.retry.busobj.annotation.FilterBusObjWithMaxAttempts;
import com.alnt.jobserver.platform.base.retry.busobj.annotation.UpdateBusObjRetryAttempts;
import com.alnt.jobserver.platform.base.retry.busobj.aspects.BusObjProactivePreventiveRetryAspect;
import com.alnt.jobserver.platform.base.retry.busobj.aspects.BusObjReactivePreventiveRetryAspect;
import com.alnt.jobserver.platform.base.retry.busobj.aspects.BusObjUpdateRetryAttemptsAspect;
import com.alnt.platform.application.injector.annotation.Module;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

@Module
public class RetryPreventionModule extends AbstractModule {
    @Override
    protected void configure() {
        BusObjProactivePreventiveRetryAspect proactivePreventiveRetryAspect = new BusObjProactivePreventiveRetryAspect();
        requestInjection(proactivePreventiveRetryAspect);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(FilterBusObjWithMaxAttempts.class),
                proactivePreventiveRetryAspect);

        BusObjReactivePreventiveRetryAspect reactivePreventiveRetryAspect = new BusObjReactivePreventiveRetryAspect();
        requestInjection(reactivePreventiveRetryAspect);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(CheckBusObjWithMaxAttempts.class),
                reactivePreventiveRetryAspect);

        BusObjUpdateRetryAttemptsAspect updateRetryAttemptsAspect = new BusObjUpdateRetryAttemptsAspect();
        requestInjection(updateRetryAttemptsAspect);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(UpdateBusObjRetryAttempts.class),
                updateRetryAttemptsAspect);
    }
}
