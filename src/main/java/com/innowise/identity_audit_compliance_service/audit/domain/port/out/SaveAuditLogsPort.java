package com.innowise.identity_audit_compliance_service.audit.domain.port.out;

import com.innowise.identity_audit_compliance_service.audit.domain.model.AuditEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface SaveAuditLogsPort {
    void saveBatch(List<AuditEvent> events);
}