package com.gigshift.allocation.shift.selection.service;

import com.gigshift.allocation.shift.catalog.model.Shift;
import com.gigshift.allocation.shift.catalog.model.ShiftStatus;
import com.gigshift.allocation.shift.catalog.repository.ShiftRepository;
import com.gigshift.allocation.shift.selection.dto.ShiftSelectionResponse;
import com.gigshift.allocation.shift.selection.model.ShiftSelection;
import com.gigshift.allocation.shift.selection.repository.ShiftSelectionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShiftSelectionService {

    private final ShiftSelectionRepository selectionRepository;
    private final ShiftRepository shiftRepository;

    public ShiftSelectionService(ShiftSelectionRepository selectionRepository,
                                 ShiftRepository shiftRepository) {
        this.selectionRepository = selectionRepository;
        this.shiftRepository = shiftRepository;
    }

    public ShiftSelectionResponse selectShift(String workerId, String shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found"));

        if (shift.getStatus() != ShiftStatus.OPEN) {
            throw new IllegalStateException("Cannot select a non-open shift");
        }

        if (selectionRepository.existsByShiftIdAndWorkerId(shiftId, workerId)) {
            // already selected; return existing
            ShiftSelection existing = selectionRepository.findByShiftIdAndWorkerId(shiftId, workerId)
                    .orElseThrow();
            return toResponse(existing);
        }

        ShiftSelection selection = new ShiftSelection();
        selection.setShiftId(shiftId);
        selection.setWorkerId(workerId);

        ShiftSelection saved = selectionRepository.save(selection);
        return toResponse(saved);
    }

    public List<ShiftSelectionResponse> getSelectionsForShift(String employerId, String shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found"));

        if (!shift.getEmployerId().equals(employerId)) {
            throw new AccessDeniedException("Not owner of this shift");
        }

        return selectionRepository.findByShiftId(shiftId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ShiftSelectionResponse> getSelectionsForWorker(String workerId) {
        return selectionRepository.findByWorkerId(workerId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ShiftSelectionResponse toResponse(ShiftSelection s) {
        return new ShiftSelectionResponse(
                s.getId(),
                s.getShiftId(),
                s.getWorkerId(),
                s.getCreatedAt()
        );
    }
}
