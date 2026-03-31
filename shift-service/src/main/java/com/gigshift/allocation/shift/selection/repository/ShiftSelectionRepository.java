package com.gigshift.allocation.shift.selection.repository;

import com.gigshift.allocation.shift.selection.model.ShiftSelection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShiftSelectionRepository extends JpaRepository<ShiftSelection, Long> {

    List<ShiftSelection> findByShiftId(String shiftId);

    List<ShiftSelection> findByWorkerId(String workerId);

    Optional<ShiftSelection> findByShiftIdAndWorkerId(String shiftId, String workerId);

    boolean existsByShiftIdAndWorkerId(String shiftId, String workerId);
}
