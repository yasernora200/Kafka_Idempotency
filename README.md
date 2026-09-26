# 🛒 Event-Driven Order & Payment Microservices

An event-driven microservices system for order processing and payment handling, built with **Spring Boot 3**, **Apache Kafka (KRaft mode)**, and **Microsoft SQL Server**.

Two independent services — `order-service` and `payment-service` — communicate asynchronously through Kafka, with idempotent transaction handling to prevent duplicate payments.

---

## 🛠 Tech Stack

| Category | Technology |
|---|---|
| Language / Framework | Java 17+, Spring Boot 3 |
| Messaging | Apache Kafka — **KRaft mode** (no ZooKeeper) |
| Database | Microsoft SQL Server |
| Persistence | Spring Data JPA / Hibernate |
| Testing Tools | Swagger (`Idempotency_swagger.json`), Azure Data Studio |

---

## 📋 Prerequisites

- [ ] Java 17+
- [ ] Apache Kafka distribution (running in KRaft mode)
- [ ] Microsoft SQL Server running locally on port `1433`
- [ ] Swagger UI to import and run `Idempotency_swagger.json` for testing

---

## 🚀 Running the Project

### 1. Start Kafka (KRaft Mode)

```bash
# Generate a Cluster ID
bin/kafka-storage.sh random-uuid

# Format the log directories (replace <CLUSTER_ID> with the value generated above)
bin/windows/kafka-storage.bat format -t <CLUSTER_ID> -c config/server.properties

# Start the broker
bin/windows/kafka-server-start.bat config/server.properties
```

Keep this terminal open — it runs the Kafka broker itself.

### 2. Set Up SQL Server

- Make sure SQL Server is running on `localhost:1433`.
- Create the database:

```sql
CREATE DATABASE bronze;
```

- Enable the `sa` login and set its password (if not already configured):

```sql
ALTER LOGIN sa ENABLE;
ALTER LOGIN sa WITH PASSWORD = 'YourPassword';
```

- Update the connection settings in `payment-service/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    url: jdbc:sqlserver://localhost:1433;databaseName=bronze;encrypt=true;trustServerCertificate=true;
    username: sa
    password: YourPassword
```

### 3. Run Order Service

```bash
cd order-service
./mvnw spring-boot:run
```

Runs on `http://localhost:8080`.

### 4. Run Payment Service

```bash
cd payment-service
./mvnw spring-boot:run
```

Runs on `http://localhost:9292`.

---

## 🧪 Testing the Flow

The API is documented via the included `Idempotency_swagger.json` file. Import it into [Swagger Editor](https://editor.swagger.io/) or your Swagger UI of choice to explore and try out the endpoints directly.

Alternatively, send a request manually to create a new order:

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/orders` |
| **Headers** | `Content-Type: application/json` |

**Body:**
```json
{
  "orderId": "ORD_3",
  "productName": "keyboard",
  "quantity": 5,
  "price": 2000
}
```

**What happens next:**
1. `order-service` publishes the event to the `ORDER_TOPIC` Kafka topic.
2. `payment-service` consumes the event, calculates the total amount, and persists the payment record.

**Verify in SQL Server:**

```sql
USE bronze;
SELECT * FROM dbo.Payment;
```

---

## ⚡ Idempotency

Duplicate payments (caused by Kafka message retries) are prevented via a deterministic `requestId`, generated from the `orderId`, combined with a **unique constraint** on that column in the `Payment` table — any retried or replayed event with the same `requestId` is rejected at the database level instead of being processed twice.
