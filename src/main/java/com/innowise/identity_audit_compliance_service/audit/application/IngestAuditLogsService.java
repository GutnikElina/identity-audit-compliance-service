package com.innowise.identity_audit_compliance_service.audit.application;

import com.innowise.identity_audit_compliance_service.audit.domain.model.AuditEvent;
import com.innowise.identity_audit_compliance_service.audit.domain.port.out.SaveAuditLogsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngestAuditLogsService {

    private final SaveAuditLogsPort saveAuditLogsPort;

    public void processAndSave(List<AuditEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        saveAuditLogsPort.saveBatch(events);
    }
}