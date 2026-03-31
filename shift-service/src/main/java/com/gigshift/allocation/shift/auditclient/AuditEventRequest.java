package com.gigshift.allocation.shift.auditclient;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditEventRequest {

    private String serviceName;       // "shift-service"
    private String entityType;        // e.g. "ASSIGNMENT"
    private String entityId;          // assignmentId, shiftId, etc.
    private String action;            // e.g. "ASSIGNMENT_CONFIRMED"
    private String performedByUserId; // worker/employer/system
    private String performedByRole;   // "WORKER", "EMPLOYER", "SYSTEM"
    private String details;           // optional JSON or text
}
