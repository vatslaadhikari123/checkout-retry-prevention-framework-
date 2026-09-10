package com.vatsla.retry.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VisitsUIDTO extends BaseDTO {

    private String visitId;
    private String visitorId;
    private int visitStatus;

    @Builder.Default
    private Set<String> badges = new HashSet<>();

    public static final int STATUS_CHECKED_IN = 10;
    public static final int STATUS_CHECKED_OUT_FINAL = 30;
}
