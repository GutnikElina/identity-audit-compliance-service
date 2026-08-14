# Техническая реализация и Архитектура

**Документ:** `docs/architecture/identity-audit-compliance-service-tech-spec.md`
**Статус:** Approved for Development
**Сервис:** identity-audit-compliance-service (Поддомен: Warehouse Cross-Docking & B2B EDI)

## 1. Технологический Стек и Зависимости

| Компонент | Технология | Назначение / Обоснование |
| :--- | :--- | :--- |
| **Runtime** | Java 21 (Virtual Threads) | Легковесная асинхронная вычитка событий аудита из Kafka. |
| **Framework** | Spring Boot 3.3+ | Интеграция со Spring Security, Spring Kafka, Keycloak Admin API. |
| **IAM Engine** | Keycloak 24+ | SSO-провайдер, OAuth2 / OpenID Connect, хранилище учетных записей. |
| **ABAC Engine** | Open Policy Agent (OPA) / Spring Security Expression | Движок вычисления атрибутивных политик доступа (Rego language или локальный Java-evaluator). |
| **Audit Database** | ClickHouse | Ультра-быстрая колоночная БД для Append-Only хранения миллиардов логов аудита с высоким сжатием. |
| **Cache** | Redis | Кеширование результатов вычисления ABAC-политек и пользовательских атрибутов. |
| **Messaging** | Apache Kafka (Avro) | Сбор событий аудита со всех 10 микросервисов системы. |

## 2. Архитектура Вычисления ABAC и Записи Аудита

Сервис реализует двухкомпонентную архитектуру:

```text
                             ┌──────────────────────────────┐
                             │    Keycloak (OAuth2 / OIDC)  │
                             └──────────────┬───────────────┘
                                            │ JWT + Claims
                                            ▼
┌─────────────────────────┐  gRPC/REST   ┌──────────────────────────────┐
│  Spring Cloud Gateway / ├─────────────►│  identity-audit-service      │
│  Any Microservice (PEP) │              │  ┌────────────────────────┐  │
└─────────────────────────┘              │  │ OPA / ABAC Engine (PDP)│  │
                                         │  └────────────────────────┘  │
                                         └──────────────────────────────┘

                                                      ▲
                                                      │ Kafka Consumer
                                                      │ (Topic: system.audit.events)
                                                      │
                                         ┌────────────┴─────────────┐
                                         │ ClickHouse (Audit Trail) │
                                         │ (Engine = MergeTree)     │
                                         └──────────────────────────┘
```

*   **Policy Decision Point (PDP):** Встроенный легкий движок вычисления правил (Open Policy Agent или кастомный Java Spring Expression Evaluator). Кеширует профили пользователей в Redis и отвечает за миллисекунды.
*   **Audit Ingestion Engine:** Читает специальные события `SystemAuditEvent` из отдельного высокоприоритетного топика Kafka и батчами записывает их в ClickHouse.

## 3. Межсервисное Взаимодействие и Интеграции

### 3.1 Схема интеграционных связей

```text
 ┌─────────────────────────────────────────────────────────────────────────────┐
 │                      ВСЕ 10 МИКРОСЕРВИСОВ ПЛАТФОРМЫ                         │
 └──────┬──────────────────────────────────────────────────────────────┬───────┘
        │                                                              │
        │ Kafka: SystemAuditEvent (Async)                              │ gRPC: CheckAccessRequest (Sync / Cached)
        ▼                                                              ▼
 ┌─────────────────────────────────────────────────────────────────────────────┐
 │                    identity-audit-compliance-service                        │
 └──────┬──────────────────────────────────────────────────────────────┬───────┘
        │                                                              │
        ▼ Batch Insert                                                 ▼ HTTP Admin API
 ┌─────────────────────────┐                                    ┌──────────────┐
 │ ClickHouse (Audit Logs) │                                    │   Keycloak   │
 └─────────────────────────┘                                    └──────────────┘
```

### 3.2 Описание контрактов взаимодействия

**Входящие асинхронные события (Kafka):**
*   `SystemAuditEvent`:
    *   **Topic:** `logistics.system.audit.v1`
    *   **Payload (Avro):** `event_id`, `timestamp`, `actor_id`, `actor_roles`, `ip_address`, `action`, `resource_type`, `resource_id`, `changes_payload_json`.

**Входящие синхронные интерфейсы (gRPC / REST):**
*   `rpc EvaluateAccess (AccessRequest) returns (AccessDecision)` — высокоскоростная проверка ABAC-прав для API Gateway или микросервисов.

## 4. Требования к Хранению Аудита (ClickHouse Guidelines)

### 4.1 Схема таблицы и Запрет Модификации (Immutability)
ClickHouse идеально подходит для аудита благодаря колоночному хранению и огромной скорости запись/чтение:

```sql
-- Таблица логов аудита
CREATE TABLE system_audit_logs (
    event_date Date DEFAULT toDate(timestamp),
    timestamp DateTime64(3, 'UTC'),
    event_id UUID,
    actor_id UUID,
    actor_role LowCardinality(String),
    action LowCardinality(String),
    resource_type LowCardinality(String),
    resource_id String,
    ip_address String,
    before_state String,
    after_state String,
    crypto_hash String -- Хэш текущей записи + SHA256(предыдущей записи)
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(event_date)
ORDER BY (resource_type, resource_id, timestamp);

-- Отключение возможности DELETE и UPDATE для обычных пользователей в ClickHouse
-- Разрешены только операция INSERT
```

## 5. Observability и Эксплуатация

*   **Metrics (Micrometer + Prometheus):**
    *   `abac_evaluation_duration_seconds` (histogram) — скорость вычисления ABAC-правил.
    *   `audit_events_ingested_total` (counter) — количество записанных аудиторских логов.
    *   `abac_access_denied_total` (counter с тегами `resource_type`, `action`) — количество заблокированных несанкционированных попыток доступа.
*   **Logging:** Минималистичное структурированное логирование работы собственного IAM-контура. Все ошибки авторизации параллельно дублируются в SIEM-систему информационной безопасности компании.
