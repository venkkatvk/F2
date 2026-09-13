# Flash Sale Engine

High-concurrency event-driven microservice for limited-time sales. It combines distributed Redis locking, Postgres-backed inventory, a transactional outbox, Kafka event streaming, and real-time SSE telemetry.

## Architecture

```
[ Client ] --> [ FlashSaleOrderController ]
                    |
                    v
         [ Idempotency + Rate Limiter ]
                    |
                    v
         [ Redisson Lock (per product) ]
                    |
                    v
         [ OrderWriteService @Transactional ]
           - decrement inventory (Postgres)
           - save order (PENDING)
           - stage outbox event (PENDING)
                    |
                    v
         [ ScheduledOutboxPoller ]
                    |
                    v
         [ Kafka: flash-sale-orders ]
                    |
                    v
         [ OrderEventConsumer ]
           - update order (CONFIRMED)
           - broadcast SSE telemetry
```

## Prerequisites

- JDK 21+
- Maven 3.8+
- Docker & Docker Compose

## Getting Started

### 1. Start infrastructure

```bash
docker-compose up -d
```

### 2. Build and run

```bash
mvn clean spring-boot:run
```

The API listens on **8080**. Actuator endpoints are on **8081**.

## Configuration

| Setting | Value |
|---------|-------|
| PostgreSQL URL | `jdbc:postgresql://localhost:5433/flashsaledb` |
| PostgreSQL user / password | `flashuser` / `flashpassword` |
| Redis | `localhost:6379` |
| Kafka | `localhost:9092` |
| API port | `8080` |
| Actuator port | `8081` |

Seed inventory: product `prod_flash_99` starts with 100 units (see `src/main/resources/data.sql`).

## API Usage

### Health check

```bash
curl http://localhost:8081/actuator/health
```

### Submit an order

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: my-unique-key-123" \
  -d '{
        "userId": "user_9918",
        "productId": "prod_flash_99",
        "quantity": 1
      }'
```

**Response (202 Accepted):**

```json
{ "orderId": "ORD-a1b2c3d4", "status": "PENDING" }
```

### HTTP status codes

| Status | Meaning |
|--------|---------|
| 202 | Order accepted, processing asynchronously |
| 400 | Validation error (missing fields, quantity < 1) |
| 409 | Insufficient stock or duplicate idempotency key in progress |
| 429 | Lock contention or rate limit exceeded |
| 503 | Circuit breaker open (downstream degraded) |

### Idempotency

Pass `X-Idempotency-Key` header with a unique value. Retries with the same key return the cached response without creating a duplicate order.

### Live telemetry (SSE)

```bash
curl -N http://localhost:8080/api/v1/telemetry/stream
```

Open the dashboard at [http://localhost:8080/dashboard.html](http://localhost:8080/dashboard.html).

## Tech Stack

- Java 21, Spring Boot 3.2.3
- PostgreSQL, Spring Data JPA
- Redis (Redisson locks + idempotency)
- Apache Kafka
- Resilience4j (rate limiter + circuit breaker)
- Spring Boot Actuator + Prometheus
- Testcontainers (integration tests)

## Running Tests

Requires Docker (Testcontainers spins up Postgres, Redis, and Kafka):

```bash
mvn test
```
