package com.gigshift.allocation.shift.catalog.controller;

import com.gigshift.allocation.shift.catalog.dto.CreateShiftRequest;
import com.gigshift.allocation.shift.catalog.dto.ShiftResponse;
import com.gigshift.allocation.shift.catalog.dto.UpdateShiftRequest;
import com.gigshift.allocation.shift.catalog.service.ShiftCatalogService;
import com.gigshift.allocation.shift.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shifts")
public class ShiftCatalogController {

    private final ShiftCatalogService shiftCatalogService;

    public ShiftCatalogController(ShiftCatalogService shiftCatalogService) {
        this.shiftCatalogService = shiftCatalogService;
    }

    @PostMapping
    public ResponseEntity<ShiftResponse> createShift(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateShiftRequest request) {

        if (principal == null || !"EMPLOYER".equals(principal.getRole())) {
            throw new AccessDeniedException("Only employers can create shifts");
        }

        String employerId = principal.getUserId();
        ShiftResponse response = shiftCatalogService.createShift(employerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ShiftResponse> listShifts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return shiftCatalogService.listShifts(PageRequest.of(page, size)).getContent();
    }

    @GetMapping("/{shiftId}")
    public ShiftResponse getShift(@PathVariable String shiftId) {
        return shiftCatalogService.getShiftById(shiftId);
    }

    @PutMapping("/{shiftId}")
    public ShiftResponse updateShift(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String shiftId,
            @Valid @RequestBody UpdateShiftRequest request) {

        if (principal == null || !"EMPLOYER".equals(principal.getRole())) {
            throw new AccessDeniedException("Only employers can update shifts");
        }
        String employerId = principal.getUserId();
        return shiftCatalogService.updateShift(employerId, shiftId, request);
    }

    @DeleteMapping("/{shiftId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelShift(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String shiftId) {

        if (principal == null || !"EMPLOYER".equals(principal.getRole())) {
            throw new AccessDeniedException("Only employers can cancel shifts");
        }
        String employerId = principal.getUserId();
        shiftCatalogService.cancelShift(employerId, shiftId);
    }
}
