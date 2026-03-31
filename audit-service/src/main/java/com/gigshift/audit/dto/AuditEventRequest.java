package com.gigshift.audit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditEventRequest {
    private String serviceName;
    private String entityType;
    private String entityId;
    private String action;
    private String performedByUserId;
    private String performedByRole;
    private String details;
}

