package com.innowise.identity_audit_compliance_service.audit.infrastructure.kafka;

import com.innowise.identity_audit_compliance_service.audit.infrastructure.kafka.avro.SystemAuditEventAvro;
import com.innowise.identity_audit_compliance_service.audit.application.IngestAuditLogsService;
import com.innowise.identity_audit_compliance_service.audit.domain.model.AuditEvent;
import com.innowise.identity_audit_compliance_service.audit.infrastructure.kafka.mapper.AuditAvroMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuditKafkaConsumer {

    private final IngestAuditLogsService ingestAuditLogsService;
    private final AuditAvroMapper auditAvroMapper;

    @KafkaListener(
            topics = "logistics.system.audit.v1",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeBatch(List<SystemAuditEventAvro> avroEvents, Acknowledgment ack) {
        log.info("Processing Kafka audit batch. Size: {}", avroEvents.size());

        if (!avroEvents.isEmpty()) {
            List<AuditEvent> domainEvents = avroEvents.stream()
                    .map(auditAvroMapper::toDomain)
                    .toList();

            ingestAuditLogsService.processAndSave(domainEvents);
        }

        ack.acknowledge();
        log.info("Batch successfully saved to ClickHouse, offset acknowledged.");
    }
}