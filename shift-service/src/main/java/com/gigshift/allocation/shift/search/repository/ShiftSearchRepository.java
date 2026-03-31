package com.gigshift.allocation.shift.search.repository;

import com.gigshift.allocation.shift.search.model.ShiftDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ShiftSearchRepository extends ElasticsearchRepository<ShiftDocument, String> {
    // we will use custom queries via QueryBuilders or @Query later
}
