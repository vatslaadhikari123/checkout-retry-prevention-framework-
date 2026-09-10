package com.vatsla.retry.service;

import com.google.inject.Singleton;
import com.vatsla.retry.model.FilterOperator;
import com.vatsla.retry.model.FilterRule;
import com.vatsla.retry.model.SearchCriteria;
import com.vatsla.retry.model.VisitsUIDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class VisitsUIService {

    private final List<VisitsUIDTO> mockDatabase = new ArrayList<>();

    public void seedData(List<VisitsUIDTO> visits) {
        mockDatabase.clear();
        mockDatabase.addAll(visits);
    }

    /**
     * Simulates a DB query that respects SearchCriteria filters.
     */
    public List<VisitsUIDTO> findAllSync(String requestId, SearchCriteria criteria) {
        return mockDatabase.stream()
                .filter(visit -> matchesCriteria(visit, criteria))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private boolean matchesCriteria(VisitsUIDTO visit, SearchCriteria criteria) {
        if (criteria == null || criteria.getFilterRules() == null) {
            return true;
        }

        for (FilterRule rule : criteria.getFilterRules()) {
            if ("id".equals(rule.getFieldName()) || "visitId".equals(rule.getFieldName())) {
                if (rule.getOperator() == FilterOperator.NOT_IN) {
                    Collection<String> excludedIds = (Collection<String>) rule.getValue();
                    if (excludedIds.contains(visit.getId())) {
                        return false; // Skip this record
                    }
                }
            }
        }
        return true;
    }
}
