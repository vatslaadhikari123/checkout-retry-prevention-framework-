package com.vatsla.retry;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.vatsla.retry.context.VisitorJobContext;
import com.vatsla.retry.model.LocationDTO;
import com.vatsla.retry.model.SearchCriteria;
import com.vatsla.retry.model.VisitsUIDTO;
import com.vatsla.retry.module.RetryPreventionModule;
import com.vatsla.retry.service.RetryAttemptsService;
import com.vatsla.retry.service.VisitorCheckoutJobService;
import com.vatsla.retry.service.VisitsUIService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CheckoutJobIntegrationTest {

    private Injector injector;
    private VisitorCheckoutJobService checkoutJobService;
    private VisitsUIService visitsUIService;
    private RetryAttemptsService retryAttemptsService;

    @Before
    public void setUp() {
        // Bootstrap Guice with the RetryPreventionModule to activate AOP interceptors
        injector = Guice.createInjector(new RetryPreventionModule());

        checkoutJobService = injector.getInstance(VisitorCheckoutJobService.class);
        visitsUIService = injector.getInstance(VisitsUIService.class);
        retryAttemptsService = injector.getInstance(RetryAttemptsService.class);

        retryAttemptsService.clear();
    }

    @After
    public void tearDown() {
        retryAttemptsService.clear();
    }

    @Test
    public void testPhase2_ReactiveAspect_IncrementsRetryAttemptOnExecution() {
        // Arrange
        VisitorJobContext context = VisitorJobContext.builder()
                .jobType("VISITOR_CHECKOUT")
                .requestId("REQ-001")
                .maxRetryLimit(3)
                .build();

        VisitsUIDTO visit = VisitsUIDTO.builder()
                .id("VISIT-101")
                .visitorId("V-101")
                .visitStatus(VisitsUIDTO.STATUS_CHECKED_IN)
                .build();

        LocationDTO location = LocationDTO.builder()
                .locationCode("LOC-A")
                .locationName("Headquarters")
                .build();

        // Act: Invoke single checkout intercepted by @UpdateBusObjRetryAttempts
        VisitsUIDTO result = checkoutJobService.doFinalCheckoutOnVisit(context, visit, location);

        // Assert: method executed and aspect post-processing saved 1 attempt
        assertNotNull(result);
        assertEquals(VisitsUIDTO.STATUS_CHECKED_OUT_FINAL, result.getVisitStatus());
        assertEquals(1, retryAttemptsService.getAttempts("VISITOR_CHECKOUT", "VISIT-101"));
    }

    @Test
    public void testPhase2_ReactiveAspect_RecordsAttemptEvenWhenOperationThrowsException() {
        // Arrange
        VisitorJobContext context = VisitorJobContext.builder()
                .jobType("VISITOR_CHECKOUT")
                .requestId("REQ-002")
                .maxRetryLimit(3)
                .build();

        VisitsUIDTO failingVisit = VisitsUIDTO.builder()
                .id("VISIT-FAIL")
                .visitorId("FAIL_PAYLOAD") // triggers exception in VisitorCheckoutJobService
                .visitStatus(VisitsUIDTO.STATUS_CHECKED_IN)
                .build();

        LocationDTO location = LocationDTO.builder().locationCode("LOC-A").build();

        // Act & Assert
        try {
            checkoutJobService.doFinalCheckoutOnVisit(context, failingVisit, location);
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            // Assert: verify aspect catch block recorded the failure before rethrowing
            assertEquals(1, retryAttemptsService.getAttempts("VISITOR_CHECKOUT", "VISIT-FAIL"));
        }
    }

    @Test
    public void testPhase1_ProactiveAspect_ExcludesEntitiesExceedingMaxRetries() {
        // Arrange
        String jobType = "VISITOR_CHECKOUT";
        VisitorJobContext context = VisitorJobContext.builder()
                .jobType(jobType)
                .requestId("REQ-003")
                .maxRetryLimit(2)
                .build();

        LocationDTO location = LocationDTO.builder().locationCode("LOC-A").build();

        // Seed 2 visits into our mock database
        VisitsUIDTO healthyVisit = VisitsUIDTO.builder()
                .id("VISIT-HEALTHY")
                .visitorId("V-HEALTHY")
                .visitStatus(VisitsUIDTO.STATUS_CHECKED_IN)
                .build();

        VisitsUIDTO exhaustedVisit = VisitsUIDTO.builder()
                .id("VISIT-EXHAUSTED")
                .visitorId("V-EXHAUSTED")
                .visitStatus(VisitsUIDTO.STATUS_CHECKED_IN)
                .build();

        visitsUIService.seedData(Arrays.asList(healthyVisit, exhaustedVisit));

        // Simulate that "VISIT-EXHAUSTED" has already failed twice (reaching max limit of 2)
        retryAttemptsService.recordAttempt("REQ-PREV-1", jobType, exhaustedVisit);
        retryAttemptsService.recordAttempt("REQ-PREV-2", jobType, exhaustedVisit);
        assertEquals(2, retryAttemptsService.getAttempts(jobType, "VISIT-EXHAUSTED"));

        SearchCriteria criteria = new SearchCriteria();

        // Act: Run batch checkout job intercepted by @FilterBusObjWithMaxAttempts
        checkoutJobService.doFinalCheckoutForAllThroughJob(context, criteria, location);

        // Assert:
        // 1. Proactive aspect mutated criteria with NOT_IN ["VISIT-EXHAUSTED"]
        // 2. Only "VISIT-HEALTHY" was processed and incremented its attempt counter
        assertEquals(1, retryAttemptsService.getAttempts(jobType, "VISIT-HEALTHY"));
        assertEquals(VisitsUIDTO.STATUS_CHECKED_OUT_FINAL, healthyVisit.getVisitStatus());

        // 3. "VISIT-EXHAUSTED" remained un-queried and was skipped entirely (still at 2 attempts)
        assertEquals(2, retryAttemptsService.getAttempts(jobType, "VISIT-EXHAUSTED"));
        assertEquals(VisitsUIDTO.STATUS_CHECKED_IN, exhaustedVisit.getVisitStatus());
    }
}
