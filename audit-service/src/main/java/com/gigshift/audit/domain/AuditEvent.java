package com.gigshift.audit.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "audit_events")
public class AuditEvent {

    @Id
    private String id;

    private String serviceName;     // "shift-service", "payment-service"
    private String entityType;      // "ASSIGNMENT", "EARNING"
    private String entityId;

    private String action;          // "ASSIGNMENT_APPROVED", "CHECK_IN", etc.
    private String performedByUserId;
    private String performedByRole; // "WORKER", "EMPLOYER", "SYSTEM"

    private String details;         // JSON or message
    private Instant createdAt = Instant.now();
}
