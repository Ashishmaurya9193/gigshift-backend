package com.gigshift.allocation.shift.selection.dto;

import java.time.Instant;

public class ShiftSelectionResponse {

    private Long id;
    private String shiftId;
    private String workerId;
    private Instant createdAt;

    public ShiftSelectionResponse(Long id, String shiftId, String workerId, Instant createdAt) {
        this.id = id;
        this.shiftId = shiftId;
        this.workerId = workerId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getShiftId() {
        return shiftId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
