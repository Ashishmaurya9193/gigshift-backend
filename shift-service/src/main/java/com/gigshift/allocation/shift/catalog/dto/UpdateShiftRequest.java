package com.gigshift.allocation.shift.catalog.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateShiftRequest {

    @Size(min = 1)
    private String title;

    @Size(min = 1)
    private String address;

    @DecimalMin("0.0")
    private Double positionAvailable;

    @Min(1)
    private Integer durationHours;

    @Size(min = 1)
    private String requiredSkills;

    @DecimalMin("0.0")
    private BigDecimal payRate;

    private Instant startTime;
    private String description;
}
