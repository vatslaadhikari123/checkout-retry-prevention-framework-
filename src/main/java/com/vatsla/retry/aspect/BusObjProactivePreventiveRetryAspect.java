package com.vatsla.retry.aspect;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.vatsla.retry.annotation.FilterBusObjWithMaxAttempts;
import com.vatsla.retry.context.BusObjRetryAwareJobContext;
import com.vatsla.retry.model.FilterOperator;
import com.vatsla.retry.model.FilterRule;
import com.vatsla.retry.model.SearchCriteria;
import com.vatsla.retry.service.RetryAttemptsService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@Singleton
public class BusObjProactivePreventiveRetryAspect implements MethodInterceptor {

    @Inject
    private RetryAttemptsService retryAttemptsService;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        FilterBusObjWithMaxAttempts annotation = method.getAnnotation(FilterBusObjWithMaxAttempts.class);
        Object[] args = invocation.getArguments();

        // 1. Extract context from positional index
        Optional<BusObjRetryAwareJobContext> optContext = getArg(
                args, annotation.retryJobContextMethodArgIndex(), BusObjRetryAwareJobContext.class);

        // 2. Extract SearchCriteria from positional index
        Optional<SearchCriteria> optCriteria = getArg(
                args, annotation.searchCriteriaMethodArgIndex(), SearchCriteria.class);

        // 3. Pre-processing: inject exclusion filters for exhausted IDs
        if (optContext.isPresent() && optCriteria.isPresent()) {
            BusObjRetryAwareJobContext context = optContext.get();
            SearchCriteria criteria = optCriteria.get();

            List<String> exhaustedIds = retryAttemptsService.findIdsExceedingMaxRetries(
                    context.getRequestId(),
                    context.getJobType(),
                    context.getMaxRetryLimit()
            );

            if (!exhaustedIds.isEmpty()) {
                criteria.addFilter(new FilterRule("id", FilterOperator.NOT_IN, exhaustedIds));
            }
        }

        // 4. Continue method execution
        return invocation.proceed();
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> getArg(Object[] args, int index, Class<T> clazz) {
        if (args != null && index >= 0 && index < args.length && clazz.isInstance(args[index])) {
            return Optional.of((T) args[index]);
        }
        return Optional.empty();
    }
}
