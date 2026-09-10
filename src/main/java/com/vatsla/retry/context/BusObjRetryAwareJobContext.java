package com.vatsla.retry.context;

import com.vatsla.retry.model.BaseDTO;

import java.util.function.Function;

public interface BusObjRetryAwareJobContext {
    String getJobType();
    int getMaxRetryLimit();
    String getRequestId();
    
    Function<BaseDTO, Boolean> getShouldIncrementAttemptsFn();
    void setShouldIncrementAttemptsFn(Function<BaseDTO, Boolean> fn);
}
