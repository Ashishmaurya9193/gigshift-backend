package com.gigshift.allocation.shift.catalog.service;

import com.gigshift.allocation.shift.catalog.dto.CreateShiftRequest;
import com.gigshift.allocation.shift.catalog.dto.ShiftResponse;
import com.gigshift.allocation.shift.catalog.dto.UpdateShiftRequest;
import com.gigshift.allocation.shift.catalog.event.ShiftEventPublisher;
import com.gigshift.allocation.shift.catalog.model.Shift;
import com.gigshift.allocation.shift.catalog.model.ShiftStatus;
import com.gigshift.allocation.shift.catalog.repository.ShiftRepository;
import com.gigshift.allocation.shift.search.service.ShiftSearchIndexer;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class ShiftCatalogService {

    private final ShiftRepository shiftRepository;
    private final ShiftEventPublisher shiftEventPublisher;
    private final ShiftSearchIndexer shiftSearchIndexer;

    public ShiftCatalogService(ShiftRepository shiftRepository,
                               ShiftEventPublisher shiftEventPublisher,
                               ShiftSearchIndexer shiftSearchIndexer) {
        this.shiftRepository = shiftRepository;
        this.shiftEventPublisher = shiftEventPublisher;
        this.shiftSearchIndexer = shiftSearchIndexer;
    }

    public ShiftResponse createShift(String employerId, CreateShiftRequest req) {
        Shift shift = new Shift();
        shift.setEmployerId(employerId);
        shift.setTitle(req.getTitle());
        shift.setAddress(req.getAddress());
        shift.setPositionAvailable(req.getPositionAvailable());
        shift.setDurationHours(req.getDurationHours());
        shift.setRequiredSkills(req.getRequiredSkills());
        shift.setPayRate(req.getPayRate());
        shift.setStartTime(req.getStartTime());
        shift.setDescription(req.getDescription());
        shift.setStatus(ShiftStatus.OPEN);

        Shift saved = shiftRepository.save(shift);
        shiftEventPublisher.publishShiftCreated(saved);
        shiftSearchIndexer.indexShift(saved);
        return toResponse(saved);
    }

    public Page<ShiftResponse> listShifts(Pageable pageable) {
        return shiftRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public ShiftResponse getShiftById(String shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found"));
        return toResponse(shift);
    }

    public ShiftResponse updateShift(String employerId, String shiftId, UpdateShiftRequest req) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found"));

        if (!shift.getEmployerId().equals(employerId)) {
            throw new AccessDeniedException("Not owner of this shift");
        }
        if (shift.getStatus() != ShiftStatus.OPEN) {
            throw new IllegalStateException("Cannot edit shift after it is claimed/started");
        }

        if (req.getTitle() != null) shift.setTitle(req.getTitle());
        if (req.getAddress() != null) shift.setAddress(req.getAddress());
        if (req.getPositionAvailable() != null) shift.setPositionAvailable(req.getPositionAvailable());
        if (req.getDurationHours() != null) shift.setDurationHours(req.getDurationHours());
        if (req.getRequiredSkills() != null) shift.setRequiredSkills(req.getRequiredSkills());
        if (req.getPayRate() != null) shift.setPayRate(req.getPayRate());
        if (req.getStartTime() != null) shift.setStartTime(req.getStartTime());
        if (req.getDescription() != null) shift.setDescription(req.getDescription());

        Shift saved = shiftRepository.save(shift);
        shiftEventPublisher.publishShiftUpdated(saved);
        shiftSearchIndexer.indexShift(saved);
        return toResponse(saved);
    }

    public void cancelShift(String employerId, String shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found"));

        if (!shift.getEmployerId().equals(employerId)) {
            throw new AccessDeniedException("Not owner of this shift");
        }

        if (shift.getStatus() == ShiftStatus.COMPLETED || shift.getStatus() == ShiftStatus.CANCELLED) {
            return;
        }

        shift.setStatus(ShiftStatus.CANCELLED);
        Shift saved = shiftRepository.save(shift);
        shiftEventPublisher.publishShiftUpdated(saved);
        shiftSearchIndexer.indexShift(saved);
    }

    private ShiftResponse toResponse(Shift s) {
        ShiftResponse r = new ShiftResponse();
        r.setShiftId(s.getShiftId());
        r.setEmployerId(s.getEmployerId());
        r.setTitle(s.getTitle());
        r.setAddress(s.getAddress());
        r.setPositionAvailable(s.getPositionAvailable());
        r.setDurationHours(s.getDurationHours());
        r.setRequiredSkills(s.getRequiredSkills());
        r.setPayRate(s.getPayRate());
        r.setStartTime(s.getStartTime());
        r.setStatus(s.getStatus());
        r.setDescription(s.getDescription());
        r.setCreatedAt(s.getCreatedAt());
        r.setUpdatedAt(s.getUpdatedAt());
        return r;
    }
}
