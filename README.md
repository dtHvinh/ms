### Microservice Demo (Service A + Service B)

#### Overview

This project demonstrates a simple event‑driven microservice architecture built with Java 17 on Apache Karaf (OSGi) and
Apache Kafka.

- Service A: HTTP API that accepts Person operations and publishes domain events to Kafka.
- Service B: Kafka consumer that discovers annotated handlers and processes those events.

Both services communicate via a single Kafka topic: `microservice-service-bridge`.

Repository layout:

- `service-a/` — REST API (Pax Web Whiteboard) + Kafka publisher
- `service-b/` — Kafka consumer with annotation‑driven handlers
- `deployments/kafka/docker-compose.yml` — Local Kafka/ZooKeeper stack for development
- `ms-test-api/` — API test collection (Bruno/OpenAPI style YAML) and environment values

Tech stack:

- Java 17, Maven 3.9+
- Apache Karaf 4.4.x, Pax Web 10.x
- Apache Kafka

#### Architecture

1. Client calls Service A HTTP endpoints (create/update/delete person).
2. Service A publishes an event to the Kafka topic `microservice-service-bridge` with a key set to the event name.
3. Service B runs a Kafka consumer loop, scans for classes annotated with `@EventHandler`, and dispatches messages to
   matching handlers based on the event key.

Event names (keys):

- `CreatePersonEvent`
- `UpdatePersonEvent`
- `DeletePersonEvent`

Topic (both services): `microservice-service-bridge` (see `ApplicationConstants.AppGlobalTopic`).

---

### Prerequisites

- Java 17 (JDK)
- Maven 3.9+
- Docker and Docker Compose (for running Kafka and containerized services)

### Quick start

#### 1) Start Kafka locally

From the project root:

```
docker compose -f deployments/kafka/docker-compose.yml up -d
```

Kafka bootstrap will be available on `localhost:9092` by default. Export it for both services:

```
export KAFKA_BOOTSTRAP_SERVER=localhost:9092
```

If you’re on Windows PowerShell:

```
$env:KAFKA_BOOTSTRAP_SERVER = "localhost:9092"
```

#### 2) Build services

Build each service module (they are independent Maven projects):

```
mvn -f service-a/pom.xml -DskipTests package
mvn -f service-b/pom.xml -DskipTests package
```

#### 3) Run with Docker (recommended)

Service A (HTTP API):

```
docker build -t ms-service-a ./service-a
docker run --rm \
  -p 8181:8181 \
  -e KAFKA_BOOTSTRAP_SERVER=${KAFKA_BOOTSTRAP_SERVER} \
  --name ms-service-a ms-service-a
```

Service B (Kafka consumer):

```
docker build -t ms-service-b ./service-b
docker run --rm \
  -e KAFKA_BOOTSTRAP_SERVER=${KAFKA_BOOTSTRAP_SERVER} \
  --name ms-service-b ms-service-b
```

Notes:

- Both Dockerfiles embed Karaf and deploy each built bundle/JAR to its `deploy/` folder, then run Karaf.
- Port `8181` is exposed in both images; only Service A uses HTTP endpoints on that port.

---

### Configuration

Common environment variable:

- `KAFKA_BOOTSTRAP_SERVER` — required by both services to connect to Kafka (e.g., `localhost:9092`).

Constants (in code):

- Topic name: `microservice-service-bridge` (`ApplicationConstants.AppGlobalTopic` in both services).

### Service A — HTTP API

Base URL (default): `http://localhost:8181`

Endpoints (paths inferred from resources):

- Health
    - `GET /api/health` — returns a simple health status.

- Person
    - `POST /api/person` — create person; publishes `CreatePersonEvent`.
    - `PUT /api/person` — update person; publishes `UpdatePersonEvent`.
    - `DELETE /api/person/{id}` — delete person; publishes `DeletePersonEvent`.

Request/Response examples

Create person:

```
POST /api/person
Content-Type: application/json

{
  "id": 1,
  "name": "Alice",
  "age": 30
}
```

Update person:

```
PUT /api/person
Content-Type: application/json

{
  "id": 1,
  "name": "Alice B.",
  "age": 31
}
```

Delete person:

```
DELETE /api/person/1
```

If `KAFKA_BOOTSTRAP_SERVER` is missing, the API will return an error indicating Kafka is not configured.

### Service B — Kafka Consumer

Service B uses `ConsumerBridge` to:

- Subscribe to the topic `microservice-service-bridge`.
- Deserialize messages (Gson) and route them by event key to handlers annotated with `@EventHandler`.

Built‑in handlers (package `com.dthvinh.libs.kafka.consumer`):

- `CreatePersonHandler` — key: `CreatePersonEvent` — payload type: `CreatePersonData { id:int, name:String, age:int }`
- `UpdatePersonHandler` — key: `UpdatePersonEvent` — payload type: `UpdatePersonData { id:int, name:String, age:int }`
- `DeletePersonHandler` — key: `DeletePersonEvent` — payload type: `String` (person id)

Adding a new handler:

1. Create a class extending `EventConsumer<YourType>`.
2. Annotate it with `@EventHandler(eventKey = "YourEventName")`.
3. Ensure your handler lives under `com.dthvinh.libs.kafka.consumer` (so it’s discovered by Reflections).

### Event contracts

All events are published to topic `microservice-service-bridge` with:

- Key: event name (`CreatePersonEvent`, `UpdatePersonEvent`, `DeletePersonEvent`).
- Value: JSON payload of the event data.

Schemas (from Service A DTOs):

- `CreatePersonEvent` value:
  ```
  { "id": number, "name": string, "age": number }
  ```
- `UpdatePersonEvent` value:
  ```
  { "id": number, "name": string, "age": number }
  ```
- `DeletePersonEvent` value:
  ```
  "<personId as string>"
  ```

### API testing

This repo provides a ready‑to‑use API test collection:

- Folder: `ms-test-api/`
    - `opencollection.yml` and environment file `environments/ms.yml`
    - Service A request definitions under `ms-test-api/service-a/`

You can import this collection with Bruno (recommended) or adapt to your favorite REST client.

### Local development tips

- If you change event names or add new handlers, ensure both producer (Service A) and consumer (Service B) agree on the
  event key and payload shape.
- When running outside Docker, start Karaf yourself or use the Maven build + Dockerfiles as the quickest path.
- For Kafka inspection, consider tools like `kafkacat` or UI tools like Redpanda Console.

### Troubleshooting

- HTTP 500 / Errors about Kafka server not set
    - Ensure `KAFKA_BOOTSTRAP_SERVER` is exported in the container or your shell.
    - Verify Kafka is running: `docker compose -f deployments/kafka/docker-compose.yml ps`.

- No events received by Service B
    - Confirm topic: `microservice-service-bridge` exists and has messages.
    - Check logs of Service B for handler registration (“Scanning for @EventHandler…” / “Registered handler …”).
    - Ensure the event key matches the handler’s `@EventHandler(eventKey=...)`.

### License

This repository is provided for demonstration and learning purposes.
