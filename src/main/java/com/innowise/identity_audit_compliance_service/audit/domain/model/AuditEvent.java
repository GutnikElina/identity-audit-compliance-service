package com.innowise.identity_audit_compliance_service.audit.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class AuditEvent {
    UUID eventId;
    Instant timestamp;
    UUID actorId;
    String actorRole;
    String action;
    String resourceType;
    String resourceId;
    String ipAddress;
    String beforeState;
    String afterState;
    String cryptoHash;
}
