package com.gigshift.allocation.shift.search.controller;

import com.gigshift.allocation.shift.catalog.dto.ShiftResponse;
import com.gigshift.allocation.shift.search.dto.ShiftSearchCriteria;
import com.gigshift.allocation.shift.search.service.ShiftSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1")
public class ShiftSearchController {

    private final ShiftSearchService searchService;

    public ShiftSearchController(ShiftSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/shifts/search")
    public Page<ShiftResponse> searchShifts(
            @RequestParam(name = "q", required = false) String keyword,
            @RequestParam(name = "skill", required = false) String skill,
            @RequestParam(name = "minDuration", required = false) Integer minDuration,
            @RequestParam(name = "maxDuration", required = false) Integer maxDuration,
            @RequestParam(name = "minPay", required = false) BigDecimal minPay,
            @RequestParam(name = "maxPay", required = false) BigDecimal maxPay,
            @RequestParam(name = "sortAsc", defaultValue = "false") boolean sortAsc,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ShiftSearchCriteria criteria = new ShiftSearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setSkill(skill);
        criteria.setMinDuration(minDuration);
        criteria.setMaxDuration(maxDuration);
        criteria.setMinPay(minPay);
        criteria.setMaxPay(maxPay);
        criteria.setSortAsc(sortAsc);

        return searchService.search(criteria, PageRequest.of(page, size));
    }
}
