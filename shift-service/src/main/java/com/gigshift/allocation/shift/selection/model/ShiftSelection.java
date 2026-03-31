package com.gigshift.allocation.shift.selection.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "shift_selections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"shift_id", "worker_id"}))
public class ShiftSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_id", nullable = false)
    private String shiftId;   // FK to Shift.shiftId (string UUID)

    @Column(name = "worker_id", nullable = false)
    private String workerId;  // from JWT

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
