package com.gigshift.audit.service;

//import com.gigshift.audit.api.dto.AuditEventRequest;
import com.gigshift.audit.dto.AuditEventRequest;

import com.gigshift.audit.domain.AuditEvent;
import com.gigshift.audit.repository.AuditEventRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditEventRepository repo;

    public AuditService(AuditEventRepository repo) {
        this.repo = repo;
    }

    public void record(AuditEventRequest req) {
        AuditEvent e = new AuditEvent();
        e.setServiceName(req.getServiceName());
        e.setEntityType(req.getEntityType());
        e.setEntityId(req.getEntityId());
        e.setAction(req.getAction());
        e.setPerformedByUserId(req.getPerformedByUserId());
        e.setPerformedByRole(req.getPerformedByRole());
        e.setDetails(req.getDetails());
        repo.save(e);
    }
}
