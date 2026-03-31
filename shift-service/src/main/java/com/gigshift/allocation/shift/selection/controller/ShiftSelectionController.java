package com.gigshift.allocation.shift.selection.controller;

import com.gigshift.allocation.shift.security.CustomUserPrincipal;
import com.gigshift.allocation.shift.selection.dto.ShiftSelectionResponse;
import com.gigshift.allocation.shift.selection.service.ShiftSelectionService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ShiftSelectionController {

    private final ShiftSelectionService selectionService;

    public ShiftSelectionController(ShiftSelectionService selectionService) {
        this.selectionService = selectionService;
    }

    // Worker applies for a shift
    @PostMapping("/shifts/{shiftId}/select")
    public ShiftSelectionResponse selectShift(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String shiftId) {

        if (principal == null || !"WORKER".equals(principal.getRole())) {
            throw new AccessDeniedException("Only workers can select shifts");
        }
        String workerId = principal.getUserId();
        return selectionService.selectShift(workerId, shiftId);
    }

    // Employer sees all selections for their shift
    @GetMapping("/shifts/{shiftId}/selections")
    public List<ShiftSelectionResponse> getSelectionsForShift(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String shiftId) {

        if (principal == null || !"EMPLOYER".equals(principal.getRole())) {
            throw new AccessDeniedException("Only employers can view selections");
        }
        String employerId = principal.getUserId();
        return selectionService.getSelectionsForShift(employerId, shiftId);
    }

    // Worker sees all their selections
    @GetMapping("/my-selections")
    public List<ShiftSelectionResponse> getSelectionsForWorker(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        if (principal == null || !"WORKER".equals(principal.getRole())) {
            throw new AccessDeniedException("Only workers can view their selections");
        }
        String workerId = principal.getUserId();
        return selectionService.getSelectionsForWorker(workerId);
    }
}

