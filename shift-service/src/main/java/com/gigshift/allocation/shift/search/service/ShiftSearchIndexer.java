package com.gigshift.allocation.shift.search.service;

import com.gigshift.allocation.shift.catalog.model.Shift;
import com.gigshift.allocation.shift.search.model.ShiftDocument;
import com.gigshift.allocation.shift.search.repository.ShiftSearchRepository;
import org.springframework.stereotype.Service;

@Service
public class ShiftSearchIndexer {

    private final ShiftSearchRepository searchRepository;

    public ShiftSearchIndexer(ShiftSearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    public void indexShift(Shift shift) {
        ShiftDocument doc = new ShiftDocument();
        doc.setId(shift.getShiftId());
        doc.setTitle(shift.getTitle());
        doc.setRequiredSkills(shift.getRequiredSkills());
        doc.setDurationHours(shift.getDurationHours());
        doc.setPayRate(shift.getPayRate());
        doc.setCreatedAt(shift.getCreatedAt());
        doc.setStatus(shift.getStatus().name());
        doc.setEmployerId(shift.getEmployerId());
        doc.setAddress(shift.getAddress());
        doc.setPositionAvailable(shift.getPositionAvailable());
        doc.setStartTime(shift.getStartTime());
        doc.setDescription(shift.getDescription());


        searchRepository.save(doc);
    }
}
