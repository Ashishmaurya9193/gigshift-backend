package com.gigshift.allocation.shift.search.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShiftSearchCriteria {
    private String keyword;
    private String skill;
    private Integer minDuration;
    private Integer maxDuration;
    private BigDecimal minPay;
    private BigDecimal maxPay;
    private boolean sortAsc = false; // false = newest first
}
