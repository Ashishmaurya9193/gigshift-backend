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
public class CreateShiftRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String address;

    @NotNull
    @DecimalMin("0.0")
    private Double positionAvailable;

    @NotNull
    @Min(1)
    private Integer durationHours;

    @NotBlank
    private String requiredSkills;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal payRate;

    @NotNull
    private Instant startTime;

    private String description;
}
