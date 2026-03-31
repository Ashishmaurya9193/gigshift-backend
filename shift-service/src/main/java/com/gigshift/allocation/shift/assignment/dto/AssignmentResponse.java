package com.gigshift.allocation.shift.assignment.dto;

import com.gigshift.allocation.shift.assignment.model.AssignmentStatus;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AssignmentResponse {

    private String assignmentId;
    private String shiftId;
    private String workerId;
    private String employerId;
    private AssignmentStatus status;
    private Instant assignedAt;

    private Instant checkInAt;
    private Instant checkOutAt;
    private Long actualDurationMinutes;
    private Double actualHoursDecimal;

    public AssignmentResponse(String assignmentId, String shiftId, String workerId,
                              String employerId, AssignmentStatus status, Instant assignedAt) {
        this.assignmentId = assignmentId;
        this.shiftId = shiftId;
        this.workerId = workerId;
        this.employerId = employerId;
        this.status = status;
        this.assignedAt = assignedAt;
    }
}
