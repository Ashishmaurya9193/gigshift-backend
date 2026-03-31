package com.gigshift.audit.api;

//import com.gigshift.audit.api.dto.AuditEventRequest;
import com.gigshift.audit.dto.AuditEventRequest;

import com.gigshift.audit.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping("/events")
    public ResponseEntity<Void> record(@RequestBody AuditEventRequest request) {
        auditService.record(request);
        return ResponseEntity.accepted().build();
    }
}
