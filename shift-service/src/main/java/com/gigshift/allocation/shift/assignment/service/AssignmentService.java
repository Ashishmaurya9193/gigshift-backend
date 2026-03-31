package com.gigshift.allocation.shift.assignment.service;

import com.gigshift.allocation.shift.assignment.dto.AssignmentResponse;
import com.gigshift.allocation.shift.assignment.model.Assignment;
import com.gigshift.allocation.shift.assignment.model.AssignmentStatus;
import com.gigshift.allocation.shift.assignment.repository.AssignmentRepository;
import com.gigshift.allocation.shift.catalog.model.Shift;
import com.gigshift.allocation.shift.catalog.model.ShiftStatus;
import com.gigshift.allocation.shift.catalog.repository.ShiftRepository;
import com.gigshift.allocation.shift.selection.model.ShiftSelection;
import com.gigshift.allocation.shift.selection.repository.ShiftSelectionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gigshift.allocation.shift.auditclient.AuditClient;
import com.gigshift.allocation.shift.auditclient.AuditEventRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ShiftRepository shiftRepository;
    private final ShiftSelectionRepository selectionRepository;
    private final AuditClient auditClient;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             ShiftRepository shiftRepository,
                             ShiftSelectionRepository selectionRepository,
                             AuditClient auditClient) {
        this.assignmentRepository = assignmentRepository;
        this.shiftRepository = shiftRepository;
        this.selectionRepository = selectionRepository;
        this.auditClient = auditClient;
    }

    @Transactional
    public AssignmentResponse confirmWorkerForShift(String employerId, String shiftId, String workerId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found"));

        if (!shift.getEmployerId().equals(employerId)) {
            throw new AccessDeniedException("Not owner of this shift");
        }
        if (shift.getStatus() != ShiftStatus.OPEN) {
            throw new IllegalStateException("Shift is not open for assignment");
        }

        ShiftSelection selection = selectionRepository
                .findByShiftIdAndWorkerId(shiftId, workerId)
                .orElseThrow(() -> new IllegalStateException("Worker did not apply for this shift"));

        Assignment assignment = new Assignment();
        assignment.setShiftId(shiftId);
        assignment.setWorkerId(workerId);
        assignment.setEmployerId(employerId);
        assignment.setStatus(AssignmentStatus.CONFIRMED);

        Assignment saved = assignmentRepository.save(assignment);

        shift.setStatus(ShiftStatus.ASSIGNED);
        shiftRepository.save(shift);

        // Audit log
        AuditEventRequest audit = new AuditEventRequest();
        audit.setServiceName("shift-service");
        audit.setEntityType("ASSIGNMENT");
        audit.setEntityId(saved.getAssignmentId());
        audit.setAction("ASSIGNMENT_CONFIRMED");
        audit.setPerformedByUserId(employerId);
        audit.setPerformedByRole("EMPLOYER");
        audit.setDetails("Worker " + workerId + " confirmed for shift " + shiftId);
        auditClient.record(audit);

        return toResponse(saved);
    }

    @Transactional
    public AssignmentResponse checkIn(String assignmentId, String workerId) {
        Assignment assignment = assignmentRepository
                .findByAssignmentIdAndWorkerId(assignmentId, workerId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found for worker"));

        if (assignment.getStatus() != AssignmentStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED assignments can be checked in");
        }

        assignment.setCheckInAt(Instant.now());
        assignment.setStatus(AssignmentStatus.INPROGRESS);

        Assignment saved = assignmentRepository.save(assignment);

        // Audit
        AuditEventRequest audit = new AuditEventRequest();
        audit.setServiceName("shift-service");
        audit.setEntityType("ASSIGNMENT");
        audit.setEntityId(saved.getAssignmentId());
        audit.setAction("ASSIGNMENT_CHECK_IN");
        audit.setPerformedByUserId(workerId);
        audit.setPerformedByRole("WORKER");
        audit.setDetails("Worker " + workerId + " checked in for shift " + saved.getShiftId());
        auditClient.record(audit);

        return toResponse(saved);
    }

    @Transactional
    public AssignmentResponse checkOut(String assignmentId, String workerId) {
        Assignment assignment = assignmentRepository
                .findByAssignmentIdAndWorkerId(assignmentId, workerId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found for worker"));

        if (assignment.getStatus() != AssignmentStatus.INPROGRESS) {
            throw new IllegalStateException("Only INPROGRESS assignments can be checked out");
        }
        if (assignment.getCheckInAt() == null) {
            throw new IllegalStateException("Cannot check-out without check-in");
        }

        assignment.setCheckOutAt(Instant.now());
        assignment.updateDuration();
        assignment.setStatus(AssignmentStatus.COMPLETED_PENDING_APPROVAL);

        Assignment saved = assignmentRepository.save(assignment);

        // Audit
        AuditEventRequest audit = new AuditEventRequest();
        audit.setServiceName("shift-service");
        audit.setEntityType("ASSIGNMENT");
        audit.setEntityId(saved.getAssignmentId());
        audit.setAction("ASSIGNMENT_CHECK_OUT");
        audit.setPerformedByUserId(workerId);
        audit.setPerformedByRole("WORKER");
        audit.setDetails("Worker " + workerId + " checked out for shift " + saved.getShiftId()
                + ", minutes=" + saved.getActualDurationMinutes());
        auditClient.record(audit);

        return toResponse(saved);
    }

    @Transactional
    public AssignmentResponse approveCompletion(String assignmentId, String employerId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        if (!assignment.getEmployerId().equals(employerId)) {
            throw new AccessDeniedException("Not owner of this assignment");
        }
        if (assignment.getStatus() != AssignmentStatus.COMPLETED_PENDING_APPROVAL) {
            throw new IllegalStateException("Assignment is not pending approval");
        }

        assignment.setStatus(AssignmentStatus.COMPLETED);
        Assignment saved = assignmentRepository.save(assignment);

        // Audit
        AuditEventRequest audit = new AuditEventRequest();
        audit.setServiceName("shift-service");
        audit.setEntityType("ASSIGNMENT");
        audit.setEntityId(saved.getAssignmentId());
        audit.setAction("ASSIGNMENT_APPROVED");
        audit.setPerformedByUserId(employerId);
        audit.setPerformedByRole("EMPLOYER");
        audit.setDetails("Employer " + employerId + " approved assignment for worker "
                + saved.getWorkerId() + " on shift " + saved.getShiftId());
        auditClient.record(audit);

        return toResponse(saved);
    }

    public AssignmentResponse getAssignment(String assignmentId) {
        Assignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
        return toResponse(a);
    }

    private AssignmentResponse toResponse(Assignment a) {
        AssignmentResponse res = new AssignmentResponse();
        res.setAssignmentId(a.getAssignmentId());
        res.setShiftId(a.getShiftId());
        res.setWorkerId(a.getWorkerId());
        res.setEmployerId(a.getEmployerId());
        res.setStatus(a.getStatus());
        res.setAssignedAt(a.getAssignedAt());

        res.setCheckInAt(a.getCheckInAt());
        res.setCheckOutAt(a.getCheckOutAt());
        res.setActualDurationMinutes(a.getActualDurationMinutes());

        if (a.getActualDurationMinutes() != null) {
            res.setActualHoursDecimal(
                    BigDecimal.valueOf(a.getActualDurationMinutes())
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
                            .doubleValue()
            );
        }

        return res;
    }

}
