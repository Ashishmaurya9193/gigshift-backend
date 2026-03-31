package com.gigshift.allocation.shift.catalog.dto;

import com.gigshift.allocation.shift.catalog.model.ShiftStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShiftResponse {

    private String shiftId;
    private String employerId;
    private String title;
    private String address;
    private Double positionAvailable;
    private Integer durationHours;
    private String requiredSkills;
    private BigDecimal payRate;
    private Instant startTime;
    private ShiftStatus status;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
