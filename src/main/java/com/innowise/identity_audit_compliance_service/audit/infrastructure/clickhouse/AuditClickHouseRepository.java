package com.innowise.identity_audit_compliance_service.audit.infrastructure.clickhouse;

import com.innowise.identity_audit_compliance_service.audit.domain.model.AuditEvent;
import com.innowise.identity_audit_compliance_service.audit.domain.port.out.SaveAuditLogsPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class AuditClickHouseRepository implements SaveAuditLogsPort {

    private final JdbcTemplate jdbcTemplate;

    public AuditClickHouseRepository(@Qualifier("clickHouseJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String INSERT_SQL = """
        INSERT INTO system_audit_logs (
            timestamp, event_id, actor_id, actor_role, action,
            resource_type, resource_id, ip_address, before_state, after_state, crypto_hash
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    @Override
    public void saveBatch(List<AuditEvent> events) {
        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                AuditEvent event = events.get(i);
                ps.setTimestamp(1, Timestamp.from(event.getTimestamp()));
                ps.setObject(2, event.getEventId());
                ps.setObject(3, event.getActorId());
                ps.setString(4, event.getActorRole());
                ps.setString(5, event.getAction());
                ps.setString(6, event.getResourceType());
                ps.setString(7, event.getResourceId());
                ps.setString(8, event.getIpAddress());
                ps.setString(9, event.getBeforeState());
                ps.setString(10, event.getAfterState());
                ps.setString(11, event.getCryptoHash());
            }

            @Override
            public int getBatchSize() {
                return events.size();
            }
        });
    }
}