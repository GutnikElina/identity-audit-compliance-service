CREATE TABLE IF NOT EXISTS system_audit_logs (
                                                 event_date Date DEFAULT toDate(timestamp),
    timestamp DateTime64(3, 'UTC'),
    event_id UUID,
    actor_id UUID,
    actor_role LowCardinality(String),
    action LowCardinality(String),
    resource_type LowCardinality(String),
    resource_id String,
    ip_address String,
    before_state Nullable(String),
    after_state Nullable(String),
    crypto_hash String
    ) ENGINE = MergeTree()
    PARTITION BY toYYYYMM(event_date)
    ORDER BY (resource_type, resource_id, timestamp);