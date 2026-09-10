package com.vatsla.retry.aspect;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.vatsla.retry.annotation.UpdateBusObjRetryAttempts;
import com.vatsla.retry.context.BusObjRetryAwareJobContext;
import com.vatsla.retry.model.BaseDTO;
import com.vatsla.retry.service.RetryAttemptsService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.Optional;

@Singleton
public class BusObjUpdateRetryAttemptsAspect implements MethodInterceptor {

    @Inject
    private RetryAttemptsService retryAttemptsService;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        UpdateBusObjRetryAttempts annotation = method.getAnnotation(UpdateBusObjRetryAttempts.class);
        Object[] args = invocation.getArguments();

        // 1. Extract context
        Optional<BusObjRetryAwareJobContext> optContext = getArg(
                args, annotation.retryJobContextMethodArgIndex(), BusObjRetryAwareJobContext.class);

        if (!optContext.isPresent()) {
            return invocation.proceed();
        }

        BusObjRetryAwareJobContext context = optContext.get();

        // 2. Extract target entity/DTO resource
        Optional<BaseDTO> optResource = getArg(
                args, annotation.retryResourceMethodArgIndex(), BaseDTO.class);

        try {
            // 3. Execute domain method (e.g., doFinalCheckoutOnVisit)
            Object returnValue = invocation.proceed();

            // 4. Post-processing: Check if attempts should be incremented
            if (optResource.isPresent() && context.getShouldIncrementAttemptsFn().apply(optResource.get())) {
                retryAttemptsService.recordAttempt(
                        context.getRequestId(),
                        context.getJobType(),
                        optResource.get()
                );
            }

            return returnValue;

        } catch (Throwable ex) {
            // 5. Post-processing on error: persist attempt counter upon failure
            if (optResource.isPresent()) {
                retryAttemptsService.recordAttempt(
                        context.getRequestId(),
                        context.getJobType(),
                        optResource.get()
                );
            }
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> getArg(Object[] args, int index, Class<T> clazz) {
        if (args != null && index >= 0 && index < args.length && clazz.isInstance(args[index])) {
            return Optional.of((T) args[index]);
        }
        return Optional.empty();
    }
}
