package com.gigshift.audit.repository;

import com.gigshift.audit.domain.AuditEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {
}
