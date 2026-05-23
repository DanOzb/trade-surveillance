# Trade Surveillance

A small Spring Boot service that ingests trades and flags suspicious activity using pluggable anomaly detectors.

## What it does

- Accepts trade events over a REST API and persists them.
- Runs each new trade through a chain of `AnomalyDetector` implementations.
- Saves any triggered alerts and returns them in the response.
- Exposes endpoints to query alerts after the fact.

Currently ships with one detector (soon more to be added):

- **PriceOutlierDetector** – flags a trade using mean and standard deviation.

## Tech stack

- Java 21
- Spring Boot 4.0.x (Web, Data JPA, Validation)
- H2 (in-memory, `create-drop`)
- Lombok
- JUnit 5, Mockito, AssertJ, MockMvc

## Running

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`. The database is recreated on each start.

## API

**[Live API documentation](https://danozb.github.io/trade-surveillance/)**

### `POST /trades`

Ingest a trade. Returns `201 Created` with the saved trade ID and any alerts triggered by detectors.

Example ingest:

```json
{
  "symbol": "ERIC",
  "price": 150.00,
  "volume": 100,
  "status": "BUY",
  "traderId": "trader-1",
  "timestamp": "2026-01-15T10:00:00Z"
}
```

Example response:

```json
{
  "tradeId": 1,
  "alerts": [
    {
      "ruleName": "PriceOutlierDetector",
      "severity": "WARNING",
      "reason": "Z score 4.21 is above threshold 3.0"
    }
  ]
}
```

### `GET /alerts`

List all alerts.

### `GET /alerts/{id}`

Fetch a single alert. Returns `404` if not found.

## Project layout

```
src/main/java/com/example/tradesurveillance/
├── Application.java
├── trade/          # Trade entity, request, repository, service, controller
├── alert/          # Alert entity, repository, service, controller, severity
├── detection/      # AnomalyDetector interface + implementations
└── common/         # Shared response/summary records
```

## Adding a new detector

1. Implement `AnomalyDetector` and annotate the class with `@Component`.
2. Return `Optional.of(alert)` when the rule fires, `Optional.empty()` otherwise.

## Tests

```bash
./mvnw test
```

Coverage includes unit tests (service + detector logic with Mockito), web layer tests (`@WebMvcTest` for controllers), and full end to end tests (`@SpringBootTest` + `MockMvc` posting trades through the real pipeline).
