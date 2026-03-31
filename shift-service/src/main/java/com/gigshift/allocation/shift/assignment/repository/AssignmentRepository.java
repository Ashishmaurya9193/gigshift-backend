package com.gigshift.allocation.shift.assignment.repository;

import com.gigshift.allocation.shift.assignment.model.Assignment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AssignmentRepository extends JpaRepository<Assignment, String> {

    List<Assignment> findByWorkerId(String workerId);

    List<Assignment> findByEmployerId(String employerId);

    Assignment findByShiftId(String shiftId);

    // in AssignmentRepository
    Optional<Assignment> findByAssignmentIdAndWorkerId(String assignmentId, String workerId);

}
