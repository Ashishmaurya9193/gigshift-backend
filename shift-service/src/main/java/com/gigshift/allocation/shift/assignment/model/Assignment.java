package com.gigshift.allocation.shift.assignment.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "assignment_id")
    private String assignmentId;

    @Setter
    @Column(name = "shift_id", nullable = false)
    private String shiftId;

    @Setter
    @Column(name = "worker_id", nullable = false)
    private String workerId;

    @Setter
    @Column(name = "employer_id", nullable = false)
    private String employerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private AssignmentStatus status = AssignmentStatus.CONFIRMED;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(name = "check_in_at")
    private Instant checkInAt;

    @Column(name = "check_out_at")
    private Instant checkOutAt;

    @Column(name = "actual_duration_minutes")
    private Long actualDurationMinutes;


    @PrePersist
    public void prePersist() {
        this.assignedAt = Instant.now();
    }

    public void updateDuration() {
        if (checkInAt != null && checkOutAt != null) {
            long minutes = Duration.between(checkInAt, checkOutAt).toMinutes();
            this.actualDurationMinutes = Math.max(minutes, 0L);
        }
    }

}
