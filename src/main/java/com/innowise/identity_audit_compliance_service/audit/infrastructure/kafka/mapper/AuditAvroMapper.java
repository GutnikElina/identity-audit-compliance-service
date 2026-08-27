package com.innowise.identity_audit_compliance_service.audit.infrastructure.kafka.mapper;

import com.innowise.identity_audit_compliance_service.audit.domain.model.AuditEvent;
import com.innowise.identity_audit_compliance_service.audit.infrastructure.kafka.avro.SystemAuditEventAvro;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class AuditAvroMapper {

    public AuditEvent toDomain(SystemAuditEventAvro avro) {
        return AuditEvent.builder()
            .eventId(UUID.fromString(avro.getEventId().toString()))
            .timestamp(Instant.ofEpochMilli(avro.getTimestamp()))
            .actorId(UUID.fromString(avro.getActorId().toString()))
            .actorRole(avro.getActorRole().toString())
            .action(avro.getAction().toString())
            .resourceType(avro.getResourceType().toString())
            .resourceId(avro.getResourceId().toString())
            .ipAddress(avro.getIpAddress().toString())
            .beforeState(
                    avro.getBeforeState() != null ? avro.getBeforeState().toString() : null)
            .afterState(
                    avro.getAfterState() != null ? avro.getAfterState().toString() : null)
            .cryptoHash(avro.getCryptoHash().toString())
            .build();
    }
}
