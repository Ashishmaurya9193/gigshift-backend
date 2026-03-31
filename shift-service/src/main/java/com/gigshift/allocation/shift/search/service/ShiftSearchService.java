package com.gigshift.allocation.shift.search.service;

import com.gigshift.allocation.shift.catalog.dto.ShiftResponse;
import com.gigshift.allocation.shift.catalog.model.ShiftStatus;
import com.gigshift.allocation.shift.search.dto.ShiftSearchCriteria;
import com.gigshift.allocation.shift.search.model.ShiftDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ShiftSearchService {

    private final ElasticsearchOperations elasticsearch;

    public ShiftSearchService(ElasticsearchOperations elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    public Page<ShiftResponse> search(ShiftSearchCriteria criteria, Pageable pageable) {
        Criteria c = new Criteria("status").is(ShiftStatus.OPEN.name());

        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            c = c.and("title").contains(criteria.getKeyword());
        }

        if (criteria.getSkill() != null && !criteria.getSkill().isBlank()) {
            c = c.and("requiredSkills").contains(criteria.getSkill());
        }

        if (criteria.getMinDuration() != null) {
            c = c.and("durationHours").greaterThanEqual(criteria.getMinDuration());
        }
        if (criteria.getMaxDuration() != null) {
            c = c.and("durationHours").lessThanEqual(criteria.getMaxDuration());
        }

        if (criteria.getMinPay() != null) {
            c = c.and("payRate").greaterThanEqual(criteria.getMinPay());
        }
        if (criteria.getMaxPay() != null) {
            c = c.and("payRate").lessThanEqual(criteria.getMaxPay());
        }

        CriteriaQuery query = new CriteriaQuery(c);
        query.addSort(Sort.by(
                criteria.isSortAsc() ? Sort.Direction.ASC : Sort.Direction.DESC,
                "createdAt"
        ));
        query.setPageable(pageable);

        SearchHits<ShiftDocument> hits = elasticsearch.search(query, ShiftDocument.class);

        var content = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(content, pageable, hits.getTotalHits());
    }

    private ShiftResponse toResponse(ShiftDocument doc) {
        ShiftResponse r = new ShiftResponse();
        r.setShiftId(doc.getId());
        r.setEmployerId(doc.getEmployerId());
        r.setTitle(doc.getTitle());
        r.setDurationHours(doc.getDurationHours());
        r.setRequiredSkills(doc.getRequiredSkills());
        r.setPayRate(doc.getPayRate());
        r.setStatus(ShiftStatus.valueOf(doc.getStatus()));
        r.setCreatedAt(doc.getCreatedAt());
        r.setAddress(doc.getAddress());
        r.setPositionAvailable(doc.getPositionAvailable());
        r.setStartTime(doc.getStartTime());
        r.setDescription(doc.getDescription());
        return r;
    }
}
