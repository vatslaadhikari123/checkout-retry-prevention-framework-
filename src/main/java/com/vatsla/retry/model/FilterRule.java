package com.vatsla.retry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterRule {
    private String fieldName;
    private FilterOperator operator;
    private Object value;
}
