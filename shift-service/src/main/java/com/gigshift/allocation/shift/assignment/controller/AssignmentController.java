package com.gigshift.allocation.shift.assignment.controller;

import com.gigshift.allocation.shift.assignment.dto.AssignmentResponse;
import com.gigshift.allocation.shift.assignment.dto.CheckInRequest;
import com.gigshift.allocation.shift.assignment.dto.CheckOutRequest;
import com.gigshift.allocation.shift.assignment.service.AssignmentService;
import com.gigshift.allocation.shift.security.CustomUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    public static class ConfirmRequest {
        private String workerId;
        public String getWorkerId() { return workerId; }
        public void setWorkerId(String workerId) { this.workerId = workerId; }
    }

    // Employer confirms one worker for a shift
    @PostMapping("/shifts/{shiftId}/confirm")
    public AssignmentResponse confirmWorker(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String shiftId,
            @RequestBody ConfirmRequest request) {

        if (principal == null || !"EMPLOYER".equals(principal.getRole())) {
            throw new AccessDeniedException("Only employers can confirm assignments");
        }
        String employerId = principal.getUserId();
        return assignmentService.confirmWorkerForShift(employerId, shiftId, request.getWorkerId());
    }

    @GetMapping("/assignments/{assignmentId}")
    public AssignmentResponse getAssignment(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String assignmentId) {
        // you can add role checks if needed
        return assignmentService.getAssignment(assignmentId);
    }

    @PostMapping("/{assignmentId}/check-in")
    public ResponseEntity<AssignmentResponse> checkIn(
            @PathVariable String assignmentId,
            @RequestBody CheckInRequest request
    ) {
        AssignmentResponse response = assignmentService.checkIn(assignmentId, request.getWorkerId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{assignmentId}/check-out")
    public ResponseEntity<AssignmentResponse> checkOut(
            @PathVariable String assignmentId,
            @RequestBody CheckOutRequest request
    ) {
        AssignmentResponse response = assignmentService.checkOut(assignmentId, request.getWorkerId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assignments/{assignmentId}/approve")
    public AssignmentResponse approveAssignment(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String assignmentId) {

        if (principal == null || !"EMPLOYER".equals(principal.getRole())) {
            throw new AccessDeniedException("Only employers can approve completion");
        }
        String employerId = principal.getUserId();
        return assignmentService.approveCompletion(assignmentId, employerId);
    }

}
