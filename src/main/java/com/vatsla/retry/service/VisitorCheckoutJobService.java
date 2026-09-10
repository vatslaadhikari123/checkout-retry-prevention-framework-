package com.vatsla.retry.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.vatsla.retry.annotation.FilterBusObjWithMaxAttempts;
import com.vatsla.retry.annotation.UpdateBusObjRetryAttempts;
import com.vatsla.retry.context.VisitorJobContext;
import com.vatsla.retry.model.LocationDTO;
import com.vatsla.retry.model.SearchCriteria;
import com.vatsla.retry.model.VisitsUIDTO;

import java.util.List;

@Singleton
public class VisitorCheckoutJobService {

    private final VisitsUIService visitsUIService;

    @Inject
    public VisitorCheckoutJobService(VisitsUIService visitsUIService) {
        this.visitsUIService = visitsUIService;
    }

    /**
     * Phase 1: Batch job method intercepted by BusObjProactivePreventiveRetryAspect.
     * Searches for records using criteria that gets filtered proactively.
     */
    @FilterBusObjWithMaxAttempts(retryJobContextMethodArgIndex = 0, searchCriteriaMethodArgIndex = 1)
    public void doFinalCheckoutForAllThroughJob(
            VisitorJobContext context,
            SearchCriteria searchCriteria,
            LocationDTO location) {

        List<VisitsUIDTO> eligibleVisits = visitsUIService.findAllSync(context.getRequestId(), searchCriteria);

        for (VisitsUIDTO visit : eligibleVisits) {
            if (visit.getVisitStatus() == VisitsUIDTO.STATUS_CHECKED_IN) {
                // Calls the intercepted single-visit checkout method
                doFinalCheckoutOnVisit(context, visit, location);
            }
        }
    }

    /**
     * Phase 2: Single-record checkout method intercepted by BusObjUpdateRetryAttemptsAspect.
     * Records an attempt after method execution completes or fails.
     */
    @UpdateBusObjRetryAttempts(retryJobContextMethodArgIndex = 0, retryResourceMethodArgIndex = 1)
    public VisitsUIDTO doFinalCheckoutOnVisit(
            VisitorJobContext context,
            VisitsUIDTO visitorVisit,
            LocationDTO location) {

        // Simulate checkout business operation
        if ("FAIL_PAYLOAD".equals(visitorVisit.getVisitorId())) {
            throw new RuntimeException("Checkout failed for invalid payload");
        }

        visitorVisit.setVisitStatus(VisitsUIDTO.STATUS_CHECKED_OUT_FINAL);
        return visitorVisit;
    }
}
