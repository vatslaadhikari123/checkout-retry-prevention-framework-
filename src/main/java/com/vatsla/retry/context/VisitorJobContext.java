package com.vatsla.retry.context;

import com.vatsla.retry.model.BaseDTO;
import lombok.Builder;
import lombok.Data;

import java.util.function.Function;

@Data
@Builder
public class VisitorJobContext implements BusObjRetryAwareJobContext {

    private String jobType;
    private int maxRetryLimit;
    private String requestId;

    @Builder.Default
    private Function<BaseDTO, Boolean> shouldIncrementAttemptsFn = dto -> true;
}
