# Drishti Backend Reference

**Project:** Drishti Criminal Network Intelligence Platform  
**Document status:** Source-aligned reference for the current backend implementation  
**Last reviewed:** 2026-11-12  
**Backend framework:** Spring Boot 4.1.1  
**Primary language:** Java 21 (configured in `pom.xml`)  
**API base URL:** `https://dristhi-backend-u6v8.onrender.com`

> This document describes the Java source currently in this repository. The controllers and DTO records are authoritative when older README/API text differs.

## 1. What This Backend Does

Drishti is a REST API and orchestration layer for an investigation intelligence platform. The backend:

- Manages investigation cases and case metadata.
- Stores persons, aliases, phones, accounts, vehicles, locations, documents, events, evidence, calls, transactions, and surveillance reports.
- Builds and queries an investigation relationship graph through Neo4j.
- Persists relational investigation data and ML results in PostgreSQL.
- Uploads and extracts text from PDF, DOCX, and text files.
- Calls external NLP, ML, and agent services through Spring WebClient.
- Provides graph, pathfinding, timeline, dashboard, and evidence-provenance APIs.
- Stores agent investigation sessions and conversation messages.

The frontend should communicate with Spring Boot only. It should not call the Python services directly.

## 2. Architecture

```mermaid
graph TD
    UI[Frontend] -->|REST JSON / multipart| API[Spring Boot API]
    API --> PG[(PostgreSQL)]
    API --> NEO[(Neo4j)]
    API --> NLP[NLP service]
    API --> ML[ML service]
    API --> AGENT[Agent service]
```

### Main package areas

| Package      | Responsibility                                           |
| ------------ | -------------------------------------------------------- |
| `controller` | HTTP routes and request/response mapping                 |
| `service`    | Business logic and orchestration                         |
| `repository` | Spring Data JPA repositories                             |
| `entity`     | PostgreSQL entity model                                  |
| `dto`        | Java record request/response contracts                   |
| `client`     | WebClient clients for external Python services           |
| `config`     | CORS, database URL normalization, application properties |
| `exception`  | API error mapping                                        |
| `util`       | ID generation and helper logic                           |

## 3. Deployment and Runtime

### Deployed backend

```text
https://dristhi-backend-u6v8.onrender.com
```

Cases endpoint:

```text
GET https://dristhi-backend-u6v8.onrender.com/api/cases
```

The root URL `/` is not the cases API. Use `/api/cases` and the other paths documented below.

Health endpoint:

```text
GET https://dristhi-backend-u6v8.onrender.com/actuator/health
```

### Local defaults

| Setting                    | Default                                     |
| -------------------------- | ------------------------------------------- |
| HTTP port                  | `8080` or the Render `PORT` value           |
| PostgreSQL URL             | `jdbc:postgresql://localhost:5432/dristhi`  |
| PostgreSQL username        | `JDBC`                                      |
| PostgreSQL password        | `JDBC`                                      |
| Neo4j URI                  | `bolt://localhost:7687`                     |
| Neo4j username             | `neo4j`                                     |
| Neo4j password             | `12345678`                                  |
| Frontend origin setting    | `http://localhost:3000`                     |
| NLP URL                    | `https://sih-criminal-nlp-api.onrender.com` |
| ML URL                     | `http://localhost:8002`                     |
| Agent URL                  | `http://localhost:8003`                     |
| JPA schema mode            | `update`                                    |
| Max upload file size       | `20MB`                                      |
| Max multipart request size | `25MB`                                      |

### Environment variables

The active configuration is in `src/main/resources/application.properties`.

```text
PORT
SPRING_DATASOURCE_URL
DB_URL
DATABASE_URL
DB_USERNAME
DATABASE_USERNAME
DB_PASSWORD
DATABASE_PASSWORD
NEO4J_URI
NEO4J_USERNAME
NEO4J_PASSWORD
FRONTEND_URL
NLP_SERVICE_URL
NLP_SERVICE_API_KEY
ML_SERVICE_URL
AGENT_SERVICE_URL
```

`SPRING_DATASOURCE_URL` takes precedence over `DB_URL`, which takes precedence over `DATABASE_URL`.

The application also normalizes Render-style `postgres://` and `postgresql://` URLs in `DatabaseConfig` into a JDBC URL and extracts embedded credentials when needed.

### Run locally

```powershell
cd D:\Tekathon\Drishti
.\mvnw.cmd spring-boot:run
```

Or with Maven installed:

```powershell
mvn spring-boot:run
```

Build and test:

```powershell
.\mvnw.cmd clean test
```

The project targets Java 21 in `pom.xml`. The application uses PostgreSQL and Neo4j by default. Tests use H2 where configured by the test profile/properties.

## 4. Frontend Connection

Use the deployed origin as a base URL and include the API path.

```javascript
const API_BASE_URL = "https://dristhi-backend-u6v8.onrender.com";

const response = await fetch(`${API_BASE_URL}/api/cases`);
if (!response.ok) {
  throw new Error(`Backend returned ${response.status}`);
}
const cases = await response.json();
```

`GET /api/cases` returns a JSON array, not an object containing a `cases` property.

```javascript
// Correct
setCases(data);

// Incorrect for this backend
setCases(data.cases);
```

Always clear loading state in `finally`:

```javascript
try {
  setLoading(true);
  const response = await fetch(`${API_BASE_URL}/api/cases`);
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  setCases(await response.json());
} catch (error) {
  setError(error.message);
} finally {
  setLoading(false);
}
```

The current CORS configuration allows all origins, methods, and headers through `allowedOriginPatterns("*")` and enables credentials.

## 5. REST API Summary

The current source exposes **71 routes**.

| Area           | Route count | Base path                                   |
| -------------- | ----------: | ------------------------------------------- |
| Cases          |           8 | `/api/cases`                                |
| Persons        |          10 | `/api/persons`                              |
| Documents      |           5 | `/api/documents` and `/api/cases`           |
| Network        |           5 | `/api/network`                              |
| Dashboard      |           4 | `/api/dashboard`                            |
| CDR            |           3 | `/api/cdr`                                  |
| Transactions   |           3 | `/api/transactions`                         |
| Vehicles       |           6 | `/api/vehicles`                             |
| Locations      |           4 | `/api/locations` and `/api/location-events` |
| Evidence       |           4 | `/api/evidence` and related resources       |
| Timeline       |           3 | `/api/*/timeline`                           |
| Surveillance   |           2 | `/api/surveillance`                         |
| Accounts       |           1 | `/api/accounts`                             |
| Phones         |           1 | `/api/phones`                               |
| Entity lookup  |           3 | `/api/entities`                             |
| Internal NLP   |           2 | `/internal/nlp`                             |
| Internal ML    |           5 | `/internal/ml`                              |
| Internal agent |           2 | `/internal/agent`                           |

Unless noted otherwise, successful JSON responses use HTTP 200. Create endpoints return HTTP 201. Delete endpoints return HTTP 204.

## 6. Case API

Controller: `CaseController`  
Base path: `/api/cases`

### `POST /api/cases`

Creates a case. The service assigns an ID and syncs it to the graph.

Request body:

```json
{
  "title": "Financial Network Investigation",
  "description": "Suspicious transfers between linked accounts",
  "status": "OPEN"
}
```

Fields:

| Field         | Type   | Required | Constraints        |
| ------------- | ------ | -------: | ------------------ |
| `title`       | string |      yes | non-blank, max 255 |
| `description` | string |       no | max 4000           |
| `status`      | string |      yes | non-blank, max 32  |

Returns `201`:

```json
{
  "caseId": "CASE4",
  "title": "Financial Network Investigation",
  "description": "Suspicious transfers between linked accounts",
  "status": "OPEN",
  "createdAt": "2026-11-12T16:33:52Z",
  "updatedAt": "2026-11-12T16:33:52Z"
}
```

### `GET /api/cases`

Lists cases sorted newest first.

Response:

```json
[
  {
    "caseId": "CASE003",
    "title": "Surveillance Follow-up",
    "status": "REVIEW_REQUIRED",
    "createdAt": "2026-11-12T16:33:52Z"
  }
]
```

### `GET /api/cases/{caseId}`

Returns the full case response, including description and `updatedAt`.

### `PUT /api/cases/{caseId}`

Updates `title`, `description`, and `status`. The request fields and validation are the same as `POST /api/cases`.

### `DELETE /api/cases/{caseId}`

Deletes the case. Returns `204` on success. Related data is not automatically deleted by the service.

### Case subresources

| Method | Path                               | Response                    |
| ------ | ---------------------------------- | --------------------------- |
| GET    | `/api/cases/{caseId}/surveillance` | surveillance response array |
| GET    | `/api/cases/{caseId}/persons`      | person response array       |
| GET    | `/api/cases/{caseId}/fir`          | filtered document list      |
| GET    | `/api/cases/{caseId}/timeline`     | timeline item array         |
| GET    | `/api/cases/{caseId}/evidence`     | evidence response array     |
| GET    | `/api/cases/{caseId}/documents`    | document list array         |

## 7. Person API

Controller: `PersonController`  
Base path: `/api/persons`

| Method | Path                                   | Description                                           |
| ------ | -------------------------------------- | ----------------------------------------------------- |
| GET    | `/api/persons`                         | List all persons                                      |
| GET    | `/api/persons/search?query={query}`    | Search names and aliases                              |
| GET    | `/api/persons/{personId}`              | Basic person record                                   |
| GET    | `/api/persons/{personId}/profile`      | Person plus phones, vehicles, accounts, related cases |
| GET    | `/api/persons/{personId}/connections`  | Direct relationship connections                       |
| GET    | `/api/persons/{personId}/calls`        | Calls involving the person's phones                   |
| GET    | `/api/persons/{personId}/transactions` | Transactions involving the person's accounts          |
| GET    | `/api/persons/{personId}/vehicles`     | Vehicles owned by the person                          |
| GET    | `/api/persons/{personId}/locations`    | Locations from surveillance records                   |
| GET    | `/api/persons/{personId}/surveillance` | Person surveillance records                           |
| GET    | `/api/persons/{personId}/timeline`     | Person timeline                                       |

Basic person response:

```json
{
  "personId": "P001",
  "name": "Rahul Sharma",
  "aliases": ["Rahul S."],
  "age": 34,
  "dateOfBirth": "1992-03-10",
  "address": "Sector 17",
  "status": "ACTIVE",
  "createdAt": "2026-11-12T16:33:52Z",
  "updatedAt": "2026-11-12T16:33:52Z"
}
```

Search response:

```json
{
  "personId": "P001",
  "name": "Rahul Sharma",
  "aliases": ["Rahul S."],
  "confidence": 0.95
}
```

Profile response fields:

```text
personId, name, aliases, age, dateOfBirth, address,
phones, vehicles, accounts, relatedCases
```

## 8. Document API

Controller: `DocumentController`  
Base path: `/api`

### Upload by case

`POST /api/cases/{caseId}/documents/upload`

Content type:

```text
multipart/form-data
```

Required part:

```text
file=<document file>
```

Optional parts:

```text
documentType=<FIR|POLICE_REPORT|...>
title=<display title>
source=<source name>
language=<en|hi|...>
```

Example:

```powershell
curl.exe -X POST `
  "https://dristhi-backend-u6v8.onrender.com/api/cases/CASE001/documents/upload" `
  -F "file=@report.pdf" `
  -F "documentType=FIR" `
  -F "title=First Information Report" `
  -F "source=Police Station" `
  -F "language=en"
```

### Direct upload

`POST /api/documents/upload`

This accepts the same multipart fields, with optional `caseId`. If no case ID is supplied, the document service can auto-create or resolve a case based on the upload metadata.

### Document retrieval

| Method | Path                            | Response                             |
| ------ | ------------------------------- | ------------------------------------ |
| GET    | `/api/documents`                | all document list items              |
| GET    | `/api/cases/{caseId}/documents` | documents for a case                 |
| GET    | `/api/documents/{documentId}`   | document metadata and extracted text |
| DELETE | `/api/documents/{documentId}`   | `204`                                |

Document list item fields:

```text
documentId, caseId, documentType, title, source, createdAt
```

Full document fields:

```text
documentId, caseId, documentType, title, text, source,
language, fileName, contentType, createdAt, updatedAt
```

Upload response fields:

```text
documentId, caseId, documentType, title, source, createdAt, nlpResult
```

Supported file extraction is implemented for PDF, DOCX, and text content through PDFBox and Apache POI dependencies.

## 9. Network and Graph API

Controller: `NetworkController`  
Base path: `/api/network`

| Method | Path                                         | Query parameters                            | Response                 |
| ------ | -------------------------------------------- | ------------------------------------------- | ------------------------ |
| GET    | `/api/network/person/{personId}`             | `depth`, default `2`                        | network nodes and edges  |
| GET    | `/api/network/entity/{entityId}`             | `depth`, default `2`                        | Cytoscape-style graph    |
| GET    | `/api/network/path`                          | `source`, `target`, `maxDepth`, default `5` | path response            |
| GET    | `/api/network/communities`                   | none                                        | community list           |
| GET    | `/api/network/relationship/{relationshipId}` | none                                        | relationship detail list |

Person network response:

```json
{
  "rootEntity": "P001",
  "nodes": [{ "id": "P001", "type": "PERSON", "label": "Rahul Sharma" }],
  "edges": [
    {
      "id": "REL001",
      "source": "P001",
      "target": "ACC101",
      "relationship": "OWNS",
      "confidence": 0.97
    }
  ]
}
```

Path response:

```json
{
  "source": "P001",
  "target": "ACC205",
  "pathFound": true,
  "paths": [
    {
      "nodes": ["P001", "ACC101", "ACC205"],
      "relationships": ["OWNS", "TRANSFERRED_TO"],
      "length": 2
    }
  ]
}
```

## 10. CDR and Phone API

### CDR routes

Controller: `CdrController`  
Base path: `/api/cdr`

| Method | Path               | Description                           |
| ------ | ------------------ | ------------------------------------- |
| POST   | `/api/cdr`         | Create one call record; returns `201` |
| POST   | `/api/cdr/bulk`    | Create a list of call records         |
| GET    | `/api/cdr/{cdrId}` | Retrieve one call record              |

Create request:

```json
{
  "callerPhoneId": "PH001",
  "receiverPhoneId": "PH002",
  "timestamp": "2026-11-12T10:15:00Z",
  "durationSeconds": 420,
  "caseId": "CASE001"
}
```

Required fields: `callerPhoneId`, `receiverPhoneId`, `timestamp`, and `caseId`. `durationSeconds` is optional.

### Phone routes

| Method | Path                          | Description             |
| ------ | ----------------------------- | ----------------------- |
| GET    | `/api/phones/{phoneId}/calls` | Calls involving a phone |

## 11. Financial Transaction and Account API

### Transaction routes

Controller: `TransactionController`  
Base path: `/api/transactions`

| Method | Path                                | Description                           |
| ------ | ----------------------------------- | ------------------------------------- |
| POST   | `/api/transactions`                 | Create one transaction; returns `201` |
| POST   | `/api/transactions/bulk`            | Create many transactions              |
| GET    | `/api/transactions/{transactionId}` | Retrieve one transaction              |

Create request:

```json
{
  "sourceAccountId": "ACC101",
  "destinationAccountId": "ACC205",
  "amount": 250000.0,
  "currency": "INR",
  "timestamp": "2026-11-12T10:30:00Z",
  "description": "Transfer",
  "caseId": "CASE002"
}
```

Required fields: `sourceAccountId`, `destinationAccountId`, `amount`, `currency`, `timestamp`, and `caseId`.

### Account route

| Method | Path                                     | Description                                             |
| ------ | ---------------------------------------- | ------------------------------------------------------- |
| GET    | `/api/accounts/{accountId}/transactions` | Transactions where the account is source or destination |

## 12. Vehicle API

Controller: `VehicleController`  
Base path: `/api/vehicles`

| Method | Path                                               | Description                    |
| ------ | -------------------------------------------------- | ------------------------------ |
| POST   | `/api/vehicles`                                    | Create vehicle; returns `201`  |
| GET    | `/api/vehicles`                                    | List vehicles                  |
| GET    | `/api/vehicles/{vehicleId}`                        | Retrieve vehicle               |
| GET    | `/api/vehicles/{vehicleId}/history`                | Surveillance and event history |
| GET    | `/api/vehicles/{vehicleId}/surveillance`           | Vehicle surveillance records   |
| GET    | `/api/vehicles/search?registration={registration}` | Search by exact registration   |
| GET    | `/api/vehicles/person/{personId}`                  | Vehicles owned by a person     |

Create request:

```json
{
  "registrationNumber": "PB10AB1234",
  "make": "BMW",
  "model": "X5",
  "ownerPersonId": "P002"
}
```

`registrationNumber` is required. Other fields are optional.

## 13. Location, Event, and Surveillance API

### Location routes

| Method | Path                                   | Description                                      |
| ------ | -------------------------------------- | ------------------------------------------------ |
| POST   | `/api/locations`                       | Create location; returns `201`                   |
| POST   | `/api/location-events`                 | Create a `VISIT` event for a person and location |
| GET    | `/api/locations/{locationId}/activity` | Surveillance activity at a location              |
| GET    | `/api/locations/{locationId}/persons`  | Distinct persons observed at a location          |

Create location request:

```json
{
  "name": "Sector 17",
  "address": "Sector 17, Chandigarh",
  "latitude": 30.7415,
  "longitude": 76.7681
}
```

Location event request:

```json
{
  "personId": "P001",
  "locationId": "LOC001",
  "timestamp": "2026-11-12T12:00:00Z",
  "source": "CCTV"
}
```

The location-event controller currently creates the event with the fixed case ID `CASE-UNKNOWN` and event type `VISIT`.

### Surveillance routes

| Method | Path                           | Description                               |
| ------ | ------------------------------ | ----------------------------------------- |
| POST   | `/api/surveillance`            | Create surveillance report; returns `201` |
| GET    | `/api/surveillance/{reportId}` | Retrieve surveillance report              |

Create surveillance request:

```json
{
  "caseId": "CASE001",
  "description": "Vehicle seen near warehouse",
  "timestamp": "2026-11-12T12:00:00Z",
  "locationId": "LOC001",
  "vehicleId": "V001",
  "personId": "P001",
  "source": "CCTV"
}
```

Required fields: `caseId` and `timestamp`. Other fields are optional.

## 14. Evidence API

Controller: `EvidenceController`  
Base path: `/api`

| Method | Path                                           | Description                       |
| ------ | ---------------------------------------------- | --------------------------------- |
| GET    | `/api/evidence/{evidenceId}`                   | Retrieve evidence                 |
| GET    | `/api/cases/{caseId}/evidence`                 | Evidence for a case               |
| GET    | `/api/entities/{entityId}/evidence`            | Evidence linked to an entity      |
| GET    | `/api/relationships/{relationshipId}/evidence` | Evidence linked to a relationship |

Evidence is created internally by current services during NLP document extraction. The DTO contains:

```text
evidenceId, caseId, type, description, sourceDocumentId,
entityId, relationshipId, sourceSpanText
```

## 15. Entity Lookup and Timeline API

### Entity lookup

| Method | Path                                   | Parameters           | Description                                                                      |
| ------ | -------------------------------------- | -------------------- | -------------------------------------------------------------------------------- |
| GET    | `/api/entities/{entityId}`             | none                 | Resolve a person, case, phone, account, vehicle, location, or other known entity |
| GET    | `/api/entities/{entityId}/connections` | `depth`, default `2` | Related entities                                                                 |
| GET    | `/api/entities/{entityId}/search`      | required `query`     | Search lookup results                                                            |

### Timeline

| Method | Path                                | Description                                         |
| ------ | ----------------------------------- | --------------------------------------------------- |
| GET    | `/api/persons/{personId}/timeline`  | Person documents, events, and surveillance timeline |
| GET    | `/api/cases/{caseId}/timeline`      | Case documents, events, and surveillance timeline   |
| GET    | `/api/entities/{entityId}/timeline` | Entity timeline                                     |

Timeline item fields:

```text
eventId, type, timestamp, description, sourceDocumentId
```

## 16. Dashboard API

Controller: `DashboardController`  
Base path: `/api/dashboard`

| Method | Path                                | Response            |
| ------ | ----------------------------------- | ------------------- |
| GET    | `/api/dashboard/summary`            | count summary       |
| GET    | `/api/dashboard/recent-cases`       | newest ten cases    |
| GET    | `/api/dashboard/anomalies`          | anomaly maps        |
| GET    | `/api/dashboard/network-statistics` | graph entity counts |

Summary response:

```json
{
  "totalCases": 3,
  "totalPersons": 10,
  "totalVehicles": 5,
  "totalPhones": 5,
  "totalAccounts": 5,
  "totalTransactions": 10,
  "totalCalls": 10,
  "totalRelationships": 5,
  "totalAnomalies": 0
}
```

Network statistics contains counts for relationships, people, phones, vehicles, and accounts.

## 17. Internal NLP API

Controller: `NlpController`  
Base path: `/internal/nlp`

These routes are intended for backend-to-service communication rather than direct browser use.

### `POST /internal/nlp/extract`

Request:

```json
{
  "documentId": "DOC001",
  "caseId": "CASE001",
  "documentType": "FIR",
  "language": "en",
  "text": "Rahul met Amit near Sector 17.",
  "metadata": {}
}
```

Required: `documentId`, `caseId`, `documentType`, `language`, and `text`.

Response fields:

```text
documentId, caseId, entities, relationships, events
```

Entity result fields:

```text
mention, type, canonicalId, confidence, evidence{text,start,end}
```

Relationship result fields:

```text
sourceId, relation, targetId, confidence, evidence
```

Event result fields:

```text
type, date, locationId, participants
```

### `POST /internal/nlp/entity-resolution`

Request:

```json
{
  "entity": { "name": "Rahul Sharma" },
  "candidateEntities": [{ "personId": "P001", "name": "Rahul Sharma" }]
}
```

Required: `entity` and a non-empty `candidateEntities` list.

Response:

```text
resolved, canonicalId, confidence, status
```

## 18. Internal ML API

Controller: `MlController`  
Base path: `/internal/ml`

### `POST /internal/ml/analyze`

Request:

```json
{
  "caseId": "CASE002",
  "entityIds": ["P001", "ACC101", "ACC205"],
  "analysisTypes": ["ANOMALIES", "KEY_ACTORS", "COMMUNITIES"]
}
```

Required: `caseId`, non-empty `entityIds`, and non-empty `analysisTypes`.

The backend forwards the request to the ML service and persists returned anomalies, key actors, communities, and potential links in PostgreSQL.

### ML result routes

| Method | Path                                       | Description                |
| ------ | ------------------------------------------ | -------------------------- |
| GET    | `/internal/ml/anomalies`                   | All persisted anomalies    |
| GET    | `/internal/ml/person/{personId}/anomalies` | Anomalies for one entity   |
| GET    | `/internal/ml/key-actors`                  | Persisted key actor scores |
| GET    | `/internal/ml/communities`                 | Persisted communities      |
| GET    | `/internal/ml/potential-links`             | Persisted predicted links  |

## 19. Internal Agent API

Controller: `AgentController`  
Base path: `/internal/agent`

### `POST /internal/agent/investigate`

Request:

```json
{
  "sessionId": "SESSION001",
  "caseId": "CASE001",
  "query": "Who connects the two locations?"
}
```

Required: all three fields.

Response fields:

```text
sessionId, caseId, answer, findings, evidence, entities, relatedCases
```

Each finding contains `type` and `description`. Each evidence item contains `documentId` and `description`.

The backend stores the session, the user message, and the assistant message after the agent service responds.

### `GET /internal/agent/session/{sessionId}`

Returns:

```text
sessionId, caseId, messages
```

Each message contains `role`, `content`, and `createdAt`.

## 20. Database Model

The current JPA model uses PostgreSQL tables created/updated by Hibernate with `spring.jpa.hibernate.ddl-auto=update`.

All entities inherit:

```text
createdAt: Instant, required, not updated after creation
updatedAt: Instant, required
```

### Core tables

| Table                  | Primary key       | Main fields                                                                         |
| ---------------------- | ----------------- | ----------------------------------------------------------------------------------- |
| `investigation_cases`  | `case_id`         | title, description, status                                                          |
| `persons`              | `person_id`       | name, aliases, age, date_of_birth, address, status, notes                           |
| `person_aliases`       | person join table | person_id, alias                                                                    |
| `phones`               | `phone_id`        | phone_number, owner_person_id, status                                               |
| `accounts`             | `account_id`      | account_number, account_type, owner_person_id, institution                          |
| `vehicles`             | `vehicle_id`      | registration_number, make, model, owner_person_id                                   |
| `locations`            | `location_id`     | name, address, latitude, longitude                                                  |
| `documents`            | `document_id`     | case_id, type, title, text, source, language, file metadata, NLP JSON               |
| `relationships`        | `relationship_id` | case_id, source_id, target_id, relationship, confidence, observations, provenance   |
| `cdr_records`          | `cdr_id`          | caller_phone_id, receiver_phone_id, timestamp, duration, case_id                    |
| `transactions`         | `transaction_id`  | source account, destination account, amount, currency, timestamp, description, case |
| `surveillance_reports` | `report_id`       | case, description, timestamp, location, vehicle, person, source                     |
| `events`               | `event_id`        | case, event type, timestamp, location, description, source document, entity, source |
| `evidence`             | `evidence_id`     | case, type, description, document, entity, relationship, source span                |

### Session, ML, and agent tables

| Table                    | Primary key    | Main fields                                                        |
| ------------------------ | -------------- | ------------------------------------------------------------------ |
| `investigation_sessions` | `session_id`   | case_id, query, answer                                             |
| `agent_messages`         | `message_id`   | session_id, role, content                                          |
| `ml_analyses`            | `analysis_id`  | case_id, result_json                                               |
| `ml_anomalies`           | `anomaly_id`   | case_id, entity_id, anomaly_type, anomaly_score, description       |
| `ml_key_actors`          | `key_actor_id` | case_id, entity_id, centrality_score, role                         |
| `ml_communities`         | `community_id` | case_id, member_ids_json                                           |
| `ml_potential_links`     | `link_id`      | case_id, source_id, target_id, predicted_relationship, probability |

IDs are application-generated strings, normally using prefixes such as `CASE`, `P`, `PH`, `ACC`, `V`, `LOC`, `DOC`, `REL`, `CDR`, `TX`, `REP`, `EV`, `MLA`, `MLAN`, `MLK`, `MLC`, and `MLPL`.

The string IDs are application references rather than database-enforced foreign keys. Most relationships between entities are represented by ID fields and graph edges.

## 21. Neo4j Graph Behavior

The backend synchronizes many created entities and relationship-like records to Neo4j through `NetworkService`.

Examples of graph relationships created by the service:

| Source operation                         | Graph relationship                             |
| ---------------------------------------- | ---------------------------------------------- |
| CDR creation                             | `CALLED` between caller and receiver phone IDs |
| Transaction creation                     | `TRANSFERRED_TO` between account IDs           |
| Person/vehicle/account/location creation | Entity node synchronization                    |
| NLP relationship extraction              | Relationship with NLP relation name            |
| Case creation                            | Case node synchronization                      |

Graph routes support depth-limited network traversal and shortest path searches. Neo4j failures may be logged or handled by service-level behavior depending on the operation, so PostgreSQL persistence and graph synchronization should be monitored separately.

## 22. Document Processing Flow

1. Client uploads a file using one of the document upload routes.
2. `DocumentService` validates/resolves the case and extracts text.
3. The backend stores a `DocumentEntity`.
4. The backend calls the configured NLP service.
5. NLP returns entities, relationships, and events.
6. Extracted persons, evidence, relationships, and timeline events are persisted.
7. Relevant entities and relationships are synchronized to Neo4j.
8. The upload response includes the document ID and NLP result.

Configured upload limits are 20 MB per file and 25 MB per multipart request.

## 23. Error Responses

The global exception handler returns a consistent object for handled errors:

```json
{
  "timestamp": "2026-11-12T16:33:52.118Z",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Case CASE999 not found",
  "path": "/api/cases/CASE999"
}
```

Error mappings:

| Exception                     | HTTP status | Error code                  |
| ----------------------------- | ----------: | --------------------------- |
| `ResourceNotFoundException`   |         404 | `NOT_FOUND`                 |
| `BadRequestException`         |         400 | `BAD_REQUEST`               |
| `ServiceUnavailableException` |         502 | service-specific error code |
| validation failure            |         400 | `VALIDATION_FAILED`         |
| unhandled exception           |         500 | `INTERNAL_SERVER_ERROR`     |

## 24. Seed Data

### Automatic application seed

`SeedDataService` creates synthetic data only when both the case table and person table are empty. It creates:

- 3 cases.
- 10 persons.
- 5 phones.
- 5 accounts.
- 5 locations.
- 5 vehicles.
- CDRs, transactions, relationships, and documents.

### Python seed scripts

Existing scripts:

- `seed_39_nirbhaya_case.py`
- `seed_aarushi_talwar_case.py`
- `seed_full_demo_case.py`

The new `seed_full_demo_case.py` is an idempotent development/demo uploader for a fictional complete case. It uses `DATABASE_URL` or `RENDER_DB_URL` and does not delete unrelated cases.

Install its dependency:

```powershell
python -m pip install psycopg2-binary
```

Run it:

```powershell
$env:DATABASE_URL = "postgresql://user:password@host:5432/database"
python seed_full_demo_case.py
```

Do not commit database credentials to source files or expose them in frontend code.

## 25. Useful Smoke Tests

```powershell
$base = "https://dristhi-backend-u6v8.onrender.com"

Invoke-WebRequest "$base/api/cases" -UseBasicParsing
Invoke-WebRequest "$base/api/persons" -UseBasicParsing
Invoke-WebRequest "$base/api/dashboard/summary" -UseBasicParsing
Invoke-WebRequest "$base/actuator/health" -UseBasicParsing
```

Using curl:

```bash
curl https://dristhi-backend-u6v8.onrender.com/api/cases
curl https://dristhi-backend-u6v8.onrender.com/api/dashboard/summary
curl https://dristhi-backend-u6v8.onrender.com/actuator/health
```

Expected cases response shape:

```json
[
  {
    "caseId": "CASE003",
    "title": "Surveillance Follow-up",
    "status": "REVIEW_REQUIRED",
    "createdAt": "2026-11-12T16:33:52.118587Z"
  }
]
```

## 26. Current Implementation Caveats

1. The root deployed URL `/` is not an API route. Frontends must call `/api/...`.
2. The current backend has no authentication or authorization layer in the visible source. Internal NLP, ML, and agent routes are reachable like regular routes unless deployment infrastructure protects them.
3. CORS is broad and credentials are enabled. Restrict allowed origins before production hardening.
4. Several entity references are string IDs rather than database foreign keys.
5. Deleting a case does not automatically cascade through every related table in the service layer.
6. `POST /api/location-events` currently uses the fixed case ID `CASE-UNKNOWN`.
7. ML community output currently wraps the stored JSON string as a single list item instead of parsing it into individual member IDs.
8. Some older documentation describes routes or response shapes that are not present in the current controllers. Always verify against the Java source.
9. The automatic Java seed runs only when both cases and persons are empty.
10. External NLP, ML, agent, PostgreSQL, and Neo4j availability can affect request latency and response status.

## 27. Source Map

| Concern                             | Source                                                                                                                  |
| ----------------------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| Application configuration           | `src/main/resources/application.properties`                                                                             |
| Maven dependencies and Java version | `pom.xml`                                                                                                               |
| Case routes                         | `src/main/java/com/project/tekathon/drishti/controller/CaseController.java`                                             |
| Person routes                       | `src/main/java/com/project/tekathon/drishti/controller/PersonController.java`                                           |
| Document routes                     | `src/main/java/com/project/tekathon/drishti/controller/DocumentController.java`                                         |
| Network routes                      | `src/main/java/com/project/tekathon/drishti/controller/NetworkController.java`                                          |
| Internal integrations               | `src/main/java/com/project/tekathon/drishti/controller/NlpController.java`, `MlController.java`, `AgentController.java` |
| Request/response records            | `src/main/java/com/project/tekathon/drishti/dto/`                                                                       |
| PostgreSQL entities                 | `src/main/java/com/project/tekathon/drishti/entity/`                                                                    |
| Repositories                        | `src/main/java/com/project/tekathon/drishti/repository/`                                                                |
| CORS                                | `src/main/java/com/project/tekathon/drishti/config/CorsConfig.java`                                                     |
| Render JDBC normalization           | `src/main/java/com/project/tekathon/drishti/config/DatabaseConfig.java`                                                 |
| Error format                        | `src/main/java/com/project/tekathon/drishti/exception/GlobalExceptionHandler.java`                                      |
| Automatic synthetic seed            | `src/main/java/com/project/tekathon/drishti/service/SeedDataService.java`                                               |
| Python database seed                | `seed_full_demo_case.py`                                                                                                |
