package com.gigshift.allocation.shift.auditclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuditClient {

    private final RestClient restClient;

    public AuditClient(@Value("${audit.service.base-url}") String baseUrl,
                       RestClient.Builder builder) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public void record(AuditEventRequest request) {
        restClient.post()
                .uri("/api/v1/audit/events")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
