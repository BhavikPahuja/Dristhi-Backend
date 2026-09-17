# Criminal Network Intelligence Platform — Spring Boot Backend

A production-ready Spring Boot backend serving as the central API gateway for an **AI-powered Criminal Network Intelligence Platform**. The system orchestrates data from PostgreSQL, Neo4j, and Python microservices (NLP, ML, and Agentic AI).

---

## Architecture Overview

```
                         React Frontend
                              |
                              | REST/JSON
                              v
                    +---------------------+
                    |     Spring Boot     |
                    | Central Backend/API |
                    +----------+----------+
                               |
              +----------------+----------------+
              |                |                |
              v                v                v
        PostgreSQL           Neo4j        Python Services
                                             |
                          +------------------+------------------+
                          |                  |                  |
                          v                  v                  v
                        NLP API            ML API          Agent/RAG API
```

**Key Principle**: React communicates ONLY with Spring Boot. Python services are accessed exclusively through Spring Boot as the orchestration layer.

---

## Features

### Investigation Case Management
- Create, read, update, delete cases
- Track case status and metadata
- Associate documents, evidence, and relationships with cases

### Document Processing
- Upload and process investigation documents (FIR, police reports, intelligence reports, witness statements, surveillance reports)
- Automatic text extraction from PDF, DOCX, TXT
- NLP extraction of entities, relationships, and events
- Full entity resolution and confidence tracking

### Entity Management
- **Persons**: Criminal profiles with aliases, contact info, addresses
- **Phones**: Phone numbers linked to persons
- **Vehicles**: Registration numbers, make/model, ownership
- **Accounts**: Bank accounts, linked to persons
- **Locations**: Geographic coordinates, names, addresses
- **Documents**: Source documents with extracted metadata
- **Events**: Investigation events (calls, meetings, transactions, vehicle sightings)

### Criminal Network Graph (Neo4j)
- Dynamic relationship mapping between entities
- Support for multiple relationship types: CALLED, MET, USED, OWNS, TRANSFERRED_TO, OBSERVED_WITH, ASSOCIATED_WITH, etc.
- Confidence scores on all relationships
- Evidence traceability for each relationship
- Network analysis: shortest paths, community detection, centrality calculations

### Call Detail Records (CDR)
- Track phone calls between persons
- Bulk import support
- Call duration, timestamps, case association

### Financial Transactions
- Transaction tracking between accounts
- Bulk import for financial networks
- Amount, currency, description metadata

### Surveillance Reports
- Record vehicle and person sightings
- Location-based surveillance tracking
- Source attribution

### Evidence Tracking
- Link evidence to entities, relationships, and documents
- Full provenance chain
- Support for cross-referencing

### Network Intelligence APIs
- **Person Network**: Get connected entities within N hops
- **Find Paths**: Discover shortest paths between persons/entities
- **Community Detection**: Identify network clusters
- **Relationship Details**: View evidence supporting relationships
- **Cytoscape-friendly format**: Direct graph visualization support

### Timeline Generation
- Chronological event reconstruction
- Multi-source evidence aggregation
- Per-person, per-case, and per-entity timelines

### Machine Learning Integration
- Anomaly detection results
- Key actor identification (graph centrality)
- Community structure analysis
- Potential link prediction
- Results stored in PostgreSQL for audit trail

### Agentic AI Integration
- Natural language investigation queries
- Multi-turn investigation sessions
- LLM-powered reasoning over investigation data
- Evidence-backed responses

### Dashboard
- Summary statistics (total cases, persons, relationships, etc.)
- Recent cases
- Anomaly feed
- Network statistics

---

## Technology Stack

### Core Framework
- **Spring Boot 4.1.1** — REST API framework
- **Java 24** — Application language
- **Maven** — Build and dependency management

### Database
- **PostgreSQL** — Structured investigation data
  - Cases, documents, persons, phones, vehicles, accounts
  - CDRs, transactions, surveillance, events, evidence
  - Entity metadata, ML results
  
- **Neo4j** — Criminal network graph
  - Person, phone, vehicle, location, account, case nodes
  - Relationship edges with confidence and evidence
  - Community detection, shortest path queries

### Data Access
- **Spring Data JPA** — PostgreSQL ORM
- **Spring Data Neo4j** — Neo4j graph access
- **Hibernate** — JPA implementation

### Integration
- **Spring WebClient** — HTTP communication with Python services
- **Spring Actuator** — Health checks and monitoring

### Serialization
- **Jackson** — JSON serialization/deserialization
- **Lombok** — Boilerplate reduction

### Testing
- **JUnit 5** — Unit testing
- **Mockito** — Mocking
- **Spring Boot Test** — Integration testing
- **MockMvc** — HTTP endpoint testing
- **H2 In-Memory Database** — Test database

### File Processing
- **Apache POI** — Office document parsing
- **Apache PDFBox** — PDF text extraction

---

## Getting Started

### Prerequisites
- Java 24+ (or OpenJDK 24)
- Maven 3.8+
- PostgreSQL 14+ (for production; H2 in-memory for local dev)
- Neo4j 4.4+ (for production; mocked for testing)
- Python 3.11+ with microservices running (optional for local dev)

### Local Development Setup

#### 1. Clone and Build
```bash
cd d:\Tekathon\Drishti
mvn clean install
```

#### 2. Configure Environment Variables (Optional)
Create a `.env` file or set environment variables:

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/criminal_intelligence
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Neo4j
NEO4J_URI=bolt://localhost:7687
NEO4J_USERNAME=neo4j
NEO4J_PASSWORD=your_password

# Python Services (optional)
NLP_SERVICE_URL=http://localhost:8001
ML_SERVICE_URL=http://localhost:8002
AGENT_SERVICE_URL=http://localhost:8003

# Frontend CORS
APP_FRONTEND_URL=http://localhost:3000
```

#### 3. Start with H2 (Local Development, No External Dependencies)
```bash
set DB_URL=jdbc:h2:mem:criminal_intelligence;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
set DB_USERNAME=sa
set DB_PASSWORD=
set SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop
mvn spring-boot:run
```

The app starts with seeded synthetic data (10 persons, 5 vehicles, 5 accounts, 10 CDRs, 10 transactions, 3 cases).

#### 4. Start with PostgreSQL & Neo4j (Production-Like)
```bash
# Start PostgreSQL (if using Docker)
docker run --name postgres -e POSTGRES_PASSWORD=postgres -d -p 5432:5432 postgres:15

# Start Neo4j (if using Docker)
docker run --name neo4j -e NEO4J_AUTH=neo4j/password -d -p 7687:7687 -p 7474:7474 neo4j:5

# Create database
psql -U postgres -c "CREATE DATABASE criminal_intelligence;"

# Start Spring Boot
mvn spring-boot:run
```

### Running Tests
```bash
mvn clean test
```

Tests use H2 in-memory database and mocked Neo4j to avoid external dependencies.

---

## API Endpoints

### Case Management
- `POST /api/cases` — Create case
- `GET /api/cases` — List cases
- `GET /api/cases/{caseId}` — Get case
- `PUT /api/cases/{caseId}` — Update case
- `DELETE /api/cases/{caseId}` — Delete case

### Document Processing
- `POST /api/cases/{caseId}/documents/upload` — Upload document
- `GET /api/cases/{caseId}/documents` — List case documents
- `GET /api/documents/{documentId}` — Get document metadata and text
- `DELETE /api/documents/{documentId}` — Delete document

### Person Profiles
- `GET /api/persons/search?query=Rahul` — Search persons
- `GET /api/persons/{personId}` — Get person basic info
- `GET /api/persons/{personId}/profile` — Get full person profile
- `GET /api/persons/{personId}/connections` — Get direct connections
- `GET /api/persons/{personId}/vehicles` — Get person's vehicles
- `GET /api/persons/{personId}/locations` — Get person's locations
- `GET /api/persons/{personId}/calls` — Get person's call history
- `GET /api/persons/{personId}/transactions` — Get person's financial transactions
- `GET /api/persons/{personId}/surveillance` — Get person's surveillance records

### Timeline
- `GET /api/persons/{personId}/timeline` — Person timeline
- `GET /api/cases/{caseId}/timeline` — Case timeline
- `GET /api/entities/{entityId}/timeline` — Generic entity timeline

### Call Detail Records
- `POST /api/cdr` — Add single CDR
- `POST /api/cdr/bulk` — Add multiple CDRs
- `GET /api/cdr/{cdrId}` — Get CDR details

### Financial Transactions
- `POST /api/transactions` — Add transaction
- `POST /api/transactions/bulk` — Bulk add transactions
- `GET /api/transactions/{transactionId}` — Get transaction

### Vehicles
- `POST /api/vehicles` — Create vehicle
- `GET /api/vehicles` — List vehicles
- `GET /api/vehicles/{vehicleId}` — Get vehicle
- `GET /api/vehicles/search?registration=PB10AB1234` — Search by registration

### Surveillance
- `POST /api/surveillance` — Record surveillance report
- `GET /api/surveillance/{reportId}` — Get report

### Network Intelligence
- `GET /api/network/person/{personId}?depth=2` — Person's network (N hops)
- `GET /api/network/entity/{entityId}?depth=2` — Generic entity network
- `GET /api/network/path?source=P001&target=P008&maxDepth=5` — Find paths
- `GET /api/network/communities` — Network communities
- `GET /api/network/relationship/{relationshipId}` — Relationship details with evidence

### Evidence
- `GET /api/cases/{caseId}/evidence` — Case evidence
- `GET /api/entities/{entityId}/evidence` — Entity evidence
- `GET /api/relationships/{relationshipId}/evidence` — Relationship evidence

### Dashboard
- `GET /api/dashboard/summary` — Statistics summary
- `GET /api/dashboard/recent-cases` — Recent cases
- `GET /api/dashboard/anomalies` — Detected anomalies
- `GET /api/dashboard/network-statistics` — Network stats

### Internal Python Service Proxies (NOT for React)
- `POST /internal/nlp/extract` — Extract entities/relationships from document
- `POST /internal/nlp/entity-resolution` — Resolve entity ambiguity
- `POST /internal/ml/analyze` — Run ML analysis (anomalies, key actors, communities)
- `GET /internal/ml/anomalies` — Retrieve anomalies
- `GET /internal/ml/person/{personId}/anomalies` — Person anomalies
- `GET /internal/ml/key-actors` — Key actors in network
- `GET /internal/ml/communities` — Communities
- `GET /internal/ml/potential-links` — Predicted links
- `POST /internal/agent/investigate` — Run agentic investigation
- `GET /internal/agent/session/{sessionId}` — Investigation session history

---

## Example Workflows

### Upload and Process a Document
```bash
# 1. Create a case
curl -X POST http://localhost:8080/api/cases \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Sector 17 Investigation",
    "description": "Investigation involving multiple connected individuals",
    "status": "OPEN"
  }'

# Response: {"caseId": "CASE001", "title": "Sector 17 Investigation", ...}

# 2. Upload a document
curl -X POST http://localhost:8080/api/cases/CASE001/documents/upload \
  -F "file=@fir.pdf" \
  -F "documentType=FIR" \
  -F "title=FIR 238" \
  -F "source=POLICE_DATABASE"

# Response includes extracted entities and NLP analysis
```

### Query Person's Network
```bash
curl http://localhost:8080/api/network/person/P001?depth=2
```

Response:
```json
{
  "rootEntity": "P001",
  "nodes": [
    {"id": "P001", "type": "PERSON", "label": "Rahul Sharma"},
    {"id": "P002", "type": "PERSON", "label": "Amit Kumar"},
    {"id": "PH001", "type": "PHONE", "label": "9876543210"}
  ],
  "edges": [
    {"source": "P001", "target": "P002", "relationship": "ASSOCIATED_WITH", "confidence": 0.91},
    {"source": "P001", "target": "PH001", "relationship": "USED", "confidence": 0.99}
  ]
}
```

### Find Path Between Persons
```bash
curl http://localhost:8080/api/network/path?source=P001&target=P008&maxDepth=5
```

### Query Person Profile
```bash
curl http://localhost:8080/api/persons/P001/profile
```

Response:
```json
{
  "personId": "P001",
  "name": "Rahul Sharma",
  "aliases": [],
  "age": 34,
  "address": "Sector 17",
  "phones": ["PH001"],
  "vehicles": ["V045"],
  "accounts": ["ACC101"],
  "relatedCases": ["CASE001", "CASE007"]
}
```

---

## Project Structure

```
src/main/java/com/project/tekathon/drishti/
├── CriminalIntelligenceApplication.java        Main entry point
│
├── config/
│   ├── WebClientConfig.java                    WebClient, ObjectMapper beans
│   ├── CorsConfig.java                         CORS configuration
│   ├── Neo4jConfig.java                        Neo4j driver config
│   ├── ErrorHandlingConfig.java                Global exception handler
│   └── ApplicationProperties.java              Configuration properties
│
├── controller/
│   ├── CaseController.java                     Case CRUD + metadata
│   ├── DocumentController.java                 Document upload/retrieval
│   ├── PersonController.java                   Person profiles + relationships
│   ├── CdrController.java                      Call detail records
│   ├── TransactionController.java              Financial transactions
│   ├── VehicleController.java                  Vehicle management
│   ├── LocationController.java                 Location and location events
│   ├── SurveillanceController.java             Surveillance report tracking
│   ├── EvidenceController.java                 Evidence queries
│   ├── TimelineController.java                 Timeline aggregation
│   ├── NetworkController.java                  Graph queries (Cytoscape format)
│   ├── EntityController.java                   Generic entity lookup
│   ├── DashboardController.java                Dashboard statistics
│   ├── NlpController.java                      NLP service proxy
│   ├── MlController.java                       ML service proxy
│   └── AgentController.java                    Agent service proxy
│
├── service/
│   ├── CaseService.java                        Case business logic
│   ├── DocumentService.java                    Document processing + NLP coordination
│   ├── PersonService.java                      Person profiles + search
│   ├── InvestigationDataService.java           CDR, transaction, surveillance, event logic
│   ├── NetworkService.java                     Neo4j graph queries and sync
│   ├── DashboardService.java                   Dashboard aggregations
│   ├── EntityLookupService.java                Generic entity lookup
│   ├── IntegrationService.java                 Python service orchestration
│   └── SeedDataService.java                    Development seed data
│
├── client/
│   ├── AbstractPythonServiceClient.java        Base HTTP client with error handling
│   ├── NlpServiceClient.java                   NLP service HTTP client
│   ├── MlServiceClient.java                    ML service HTTP client
│   └── AgentServiceClient.java                 Agent service HTTP client
│
├── dto/
│   ├── request/
│   │   └── [All request DTOs]
│   └── response/
│       └── [All response DTOs]
│
├── entity/
│   ├── CaseEntity.java                         JPA entity
│   ├── DocumentEntity.java                     JPA entity
│   ├── PersonEntity.java                       JPA entity
│   ├── PhoneEntity.java                        JPA entity
│   ├── VehicleEntity.java                      JPA entity
│   ├── AccountEntity.java                      JPA entity
│   ├── LocationEntity.java                     JPA entity
│   ├── CdrEntity.java                          JPA entity
│   ├── TransactionEntity.java                  JPA entity
│   ├── SurveillanceReportEntity.java           JPA entity
│   ├── EventEntity.java                        JPA entity
│   ├── EvidenceEntity.java                     JPA entity
│   ├── RelationshipEntity.java                 JPA entity
│   ├── MlAnomalyEntity.java                    JPA entity
│   ├── MlKeyActorEntity.java                   JPA entity
│   ├── MlCommunityEntity.java                  JPA entity
│   ├── MlPotentialLinkEntity.java              JPA entity
│   ├── MlAnalysisEntity.java                   JPA entity
│   ├── InvestigationSessionEntity.java         JPA entity (Agent sessions)
│   └── AgentMessageEntity.java                 JPA entity (Agent conversation)
│
├── repository/
│   ├── CaseRepository.java                     Spring Data JPA
│   ├── DocumentRepository.java
│   ├── PersonRepository.java
│   ├── [All other repositories]
│   └── Neo4j graph repositories
│
├── exception/
│   ├── GlobalExceptionHandler.java             @RestControllerAdvice
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── [Other custom exceptions]
│
└── util/
    ├── IdGenerator.java                        ID generation (P001, CASE001, etc)
    └── TextExtractionUtils.java                PDF/DOCX/TXT extraction
```

---

## Database Schema Highlights

### PostgreSQL Tables
- `case` — Investigation cases
- `document` — Uploaded documents (text + metadata)
- `person` — Individual entity records
- `phone` — Phone numbers linked to persons
- `vehicle` — Vehicles with registration
- `account` — Bank/financial accounts
- `location` — Geographic locations
- `cdr` — Call detail records
- `transaction` — Financial transactions
- `surveillance_report` — Vehicle/person sightings
- `event` — Investigation events
- `evidence` — Evidence with source traceability
- `relationship` — Entity relationships (copies of Neo4j edges)
- `ml_anomaly` — ML-detected anomalies
- `ml_key_actor` — High-centrality entities
- `ml_community` — Network communities
- `ml_potential_link` — Link predictions
- `investigation_session` — Agent investigation sessions
- `agent_message` — Agent conversation history

### Neo4j Graph Model
**Nodes:**
- `:PERSON {id, name}`
- `:PHONE {id, number}`
- `:VEHICLE {id, registration}`
- `:ACCOUNT {id, number}`
- `:LOCATION {id, name, latitude, longitude}`
- `:DOCUMENT {id, title}`
- `:CASE {id, title}`

**Relationships:**
- `(person)-[:CALLED]->(person)` — Phone calls
- `(person)-[:MET]->(person)` — Meetings
- `(person)-[:TRANSFERRED_TO]->(person)` via accounts — Transactions
- `(person)-[:USED]->(phone)` — Phone ownership
- `(person)-[:OWNS]->(vehicle)` — Vehicle ownership
- `(person)-[:WORKS_FOR]->(organization)` — Employment
- `(person)-[:ASSOCIATED_WITH]->(person)` — General association
- `(person)-[:VISITED]->(location)` — Location visits
- All relationships include: `confidence`, `firstObserved`, `lastObserved`, `supportText`, evidence references

---

## Seed Data

On startup (with DDL create-drop), the backend seeds development data:

**Persons (10):**
- P001 Rahul Sharma
- P002 Amit Kumar
- P003 Neha Singh
- P004 Vikram Patel
- P005 Sanjay Verma
- P006 Priya Rao
- P007 Arjun Mehta
- P008 Karan Gill
- P009 Simran Kaur
- P010 Deepak Joshi

**Phones (5):** PH001–PH005 linked to persons

**Vehicles (5):** BMW X5, Toyota Corolla, Honda City, etc.

**Accounts (5):** ACC101–ACC501 linked to persons

**CDRs (10):** Call sequences showing communication patterns

**Transactions (10):** Financial transfers between accounts

**Cases (3):**
- CASE001 Sector 17 Investigation
- CASE002 Financial Network Probe
- CASE003 Surveillance Follow-up

**Documents (5):** FIR, police reports, intelligence reports, witness statements

**Relationships (5):** MET, CALLED, TRANSFERRED_TO, USED, etc.

---

## Configuration

### application.properties

```properties
# App
spring.application.name=criminal-network-intelligence
server.port=8080

# PostgreSQL
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/criminal_intelligence}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Neo4j
spring.neo4j.uri=${NEO4J_URI:bolt://localhost:7687}
spring.neo4j.authentication.username=${NEO4J_USERNAME:neo4j}
spring.neo4j.authentication.password=${NEO4J_PASSWORD:password}

# Python Services
nlp.service.url=${NLP_SERVICE_URL:http://localhost:8001}
ml.service.url=${ML_SERVICE_URL:http://localhost:8002}
agent.service.url=${AGENT_SERVICE_URL:http://localhost:8003}

# Frontend CORS
app.frontend-url=${APP_FRONTEND_URL:http://localhost:3000}

# File Upload
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=25MB

# Actuator
management.endpoints.web.exposure.include=health,info
```

---

## Error Handling

All endpoints return consistent error responses:

```json
{
  "timestamp": "2026-09-16T12:30:00Z",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Person P001 not found",
  "path": "/api/persons/P001"
}
```

Service unavailability is handled gracefully:

```json
{
  "timestamp": "2026-09-16T12:30:00Z",
  "status": 502,
  "error": "NLP_SERVICE_UNAVAILABLE",
  "message": "NLP service is currently unavailable",
  "path": "/api/cases/CASE001/documents/upload"
}
```

---

## Key Design Decisions

### 1. ID Scheme
All entities use stable, non-sequential IDs:
- Persons: `P001`, `P002`, ...
- Cases: `CASE001`, `CASE002`, ...
- Documents: `DOC001`, ...
- Relationships: `REL001`, ...

This avoids name-based collision and enforces explicit entity resolution.

### 2. DTO Pattern
JPA entities are never exposed directly from REST endpoints. All responses use dedicated DTOs to maintain API stability.

### 3. Evidence Traceability
Every relationship, anomaly, and link includes:
- Source document references
- Supporting text spans
- Confidence scores
- First/last observed timestamps

This ensures investigative leads can be traced back to source data.

### 4. Neo4j as Single Source of Truth for Graphs
PostgreSQL stores structural facts (calls, transactions, vehicles). Neo4j stores the investigative network graph and performs graph analytics. Both are kept in sync.

### 5. Python Service Abstraction
Spring Boot never exposes Python service failures to the React frontend. Python clients handle timeouts, retries, and errors gracefully.

### 6. Entity Resolution with Confidence
Entity matching is conservative. Uncertain matches return status `REVIEW_REQUIRED` instead of silently merging entities. This prevents investigative errors.

---

## Performance Considerations

- **Graph queries**: Neo4j handles shortest paths, community detection, and multi-hop traversals efficiently
- **CDR/Transaction bulk import**: Batch endpoints accept 100s of records in single request
- **Document processing**: Async NLP extraction (could be offloaded to message queue in production)
- **Caching**: Consider Redis for frequently-accessed person profiles, case metadata
- **Pagination**: Timeline and evidence results should be paginated in production

---

## Security Notes

### Current (MVP)
- No authentication/authorization (as requested)
- No API key validation
- No request rate limiting

### For Production
- Add Spring Security with JWT or OAuth2
- Implement role-based access control (RBAC)
- Add request rate limiting (Spring Cloud)
- Enable HTTPS
- Audit all data modifications
- Encrypt sensitive data (SSNs, account numbers)
- Use environment variables (never hardcode credentials)

---

## Troubleshooting

### Application fails to start with PostgreSQL error
→ Ensure PostgreSQL is running and database `criminal_intelligence` exists:
```bash
psql -U postgres -c "CREATE DATABASE criminal_intelligence;"
```

### Neo4j connection timeout
→ Use H2 in-memory for local dev (Neo4j mocked in tests):
```bash
set DB_URL=jdbc:h2:mem:criminal_intelligence;MODE=PostgreSQL
mvn spring-boot:run
```

### Python service returns 502 error
→ Check that Python microservices are running:
```bash
curl http://localhost:8001/health  # NLP
curl http://localhost:8002/health  # ML
curl http://localhost:8003/health  # Agent
```

### Document upload fails with "No qualifying bean"
→ Ensure Jackson ObjectMapper is available:
```properties
# In application.properties:
spring.jackson.serialization.write-dates-as-timestamps=false
```

---

## Testing

Run all tests:
```bash
mvn clean test
```

Tests use:
- **H2 in-memory** database (PostgreSQL mode)
- **Mocked Neo4jClient** (no Neo4j required)
- **MockMvc** for endpoint testing
- **Mockito** for service mocking

---

## Next Steps (Future Enhancement)

1. **Authentication & Authorization**
   - Spring Security + JWT
   - Role-based access (investigator, analyst, admin)

2. **Async Processing**
   - Message queue (Kafka/RabbitMQ) for document processing
   - NLP extraction jobs

3. **Caching**
   - Redis for person profiles, case metadata
   - Query result caching

4. **Real-time Updates**
   - WebSocket support for investigation session streaming
   - Agent response streaming

5. **Advanced Analytics**
   - Time-series anomaly detection
   - Predictive link scoring improvements
   - Community evolution tracking

6. **Audit & Compliance**
   - Full audit trail (who accessed what, when)
   - Data retention policies
   - GDPR compliance

7. **Deployment**
   - Docker containerization
   - Kubernetes manifests
   - CI/CD pipeline (GitHub Actions, GitLab CI)

---

## Contributing

Code Style:
- Follow Spring Boot conventions
- Use Lombok for boilerplate reduction
- Keep services focused and single-responsibility
- Write integration tests for new endpoints
- Document complex business logic

---

## License

This project is part of the Tekathon 2026 hackathon.

---

## Support

For issues or questions:
1. Check troubleshooting section above
2. Review Spring Boot logs for detailed error messages
3. Verify all services are running (database, Neo4j, Python microservices if used)

---

**Last Updated**: 2026-09-16  
**Backend Version**: 1.0  
**Status**: ✅ Production-Ready MVP
