package com.vatsla.retry.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchCriteria {
    private List<FilterRule> filterRules = new ArrayList<>();

    public void addFilter(FilterRule rule) {
        this.filterRules.add(rule);
    }
}
