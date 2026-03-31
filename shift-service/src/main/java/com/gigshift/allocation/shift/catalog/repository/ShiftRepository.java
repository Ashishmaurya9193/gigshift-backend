package com.gigshift.allocation.shift.catalog.repository;

import com.gigshift.allocation.shift.catalog.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, String> {
    // later you can add custom queries if needed
}
