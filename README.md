# Repifuzz Assignment

[![Build](https://img.shields.io/badge/build-passing-brightgreen)]() [![Tests](https://img.shields.io/badge/tests-%E2%9C%93-brightgreen)]() [![Coverage](https://img.shields.io/badge/coverage-%3F%25-yellow)]() [![License](https://img.shields.io/badge/license-MIT-blue)]()

A lightweight incident-management microservice for reporting and tracking incidents. Reporters can create incidents; analysts can review, assign, and resolve them. The service exposes REST APIs secured by JWT and includes unit, controller, and security tests.

## Contents
- [Features](#features)
- [Quick Start](#quick-start-development)
- [Example API Flows](#example-api-flows-representative)
- [Incident Lifecycle](#incident-lifecycle-status-transitions--apis)
- [Testing & Debugging](#testing--debugging-concise)
- [Configuration](#configuration-test-defaults)
- [Documentation & Supporting Files](#documentation--supporting-files-recommended)
- [Contributing](#contributing)
- [Maintainers & Support](#maintainers--support)
- [License](#license)

---

## Features
- User registration and JWT-based authentication
- Create and read incidents with JSON details
- Role-based access control (REPORTER / ANALYST)
- Audit logging for lifecycle changes
- Unit, controller, and security tests (JUnit 5 + Mockito, MockMvc)

---

## Quick start (development)
Requirements: JDK 17, Gradle wrapper

1. **Build & run**
    - Start the application:
      ```bash
      ./gradlew bootRun
      ```
2. **Run tests**
    - Execute test suite:
      ```bash
      ./gradlew test
      ```
3. **View test report**
    - Open: `build/reports/tests/test/index.html`

---

## Example API flows (representative)

### 1) Register (create user)
**Request:**
```bash
curl -X POST http://localhost:8080/api/ims/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"reporter","email":"rep@example.com","password":"P@ssw0rd"}'
```
**Response (201):**
```json
{
  "id": 7,
  "username": "reporter",
  "email": "rep@example.com",
  "role": "REPORTER"
}
```

### 2) Login (get JWT)
**Request:**
```bash
curl -X POST http://localhost:8080/api/ims/user/login \
  -H "Content-Type: application/json" \
  -d '{"email":"rep@example.com","password":"P@ssw0rd"}'
```
**Response (200):**
```json
{ "token": "<jwt>" }
```

### 3) Create incident (authenticated)
**Request:**
```bash
curl -X POST http://localhost:8080/api/ims/incidents \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt>" \
  -d '{"incidentType":"SECURITY","description":"desc","details": {"meta":"value"}}'
```
**Response:**
```json
{
  "id": 123,
  "incidentId": "RMG01234-2026",
  "reporterUserId": 7,
  "status": "OPEN",
  "createdAt": "2026-08-28T12:34:56"
}
```

### 4) Read incident
**Request:**
```bash
curl -X GET "http://localhost:8080/api/ims/incidents/RMG01234-2026" \
  -H "Authorization: Bearer <jwt>"
```
- **200 OK** for allowed users
- **403 Forbidden** if unauthorized
- **404 Not Found** if missing

---

## Incident lifecycle (status transitions & APIs)

The system tracks incident lifecycle changes and records each change in an audit log.

### Key status values
- `OPEN` — newly created, untriaged
- `ASSIGNED` — assigned to an analyst
- `IN_PROGRESS` — investigation/mitigation in progress
- `RESOLVED` — issue resolved with summary
- `CLOSED` — verified and closed
- `REOPENED` — reopened after being resolved/closed

### Allowed high-level transitions
- `OPEN` → `ASSIGNED` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`
- `RESOLVED`/`CLOSED` → `REOPENED` → (follow triage flow)
- Invalid/out-of-order transitions return `409 Conflict`.

### Role rules (summary)
* **REPORTER**
    * Create incidents
    * Read their own incidents
    * Request reopen (depends on policy)
* **ANALYST**
    * Read any incident
    * Assign incidents
    * Change status, resolve, and close incidents
    * Add resolution details

### Representative lifecycle endpoints
* **Assign**
    * `PATCH /api/ims/incidents/{incidentId}/assign`
    * Body: `{ "assignedUserId": 42 }`
    * Auth: `ANALYST`
    * Response: updated `IncidentResponse`
* **Change status**
    * `PATCH /api/ims/incidents/{incidentId}/status`
    * Body: `{ "status": "IN_PROGRESS", "reason": "Start investigation" }`
    * Auth: `ANALYST` (or policy as configured)
    * Validates transitions, returns `409` for invalid ones
* **Resolve**
    * `POST /api/ims/incidents/{incidentId}/resolve`
    * Body: `{ "resolutionType": "FIXED", "resolutionSummary": "Patched service" }`
    * Side-effects: sets `resolvedAt`, `resolvedByUserId`, `resolutionSummary`
* **Reopen**
    * `POST /api/ims/incidents/{incidentId}/reopen`
    * Body: `{ "reason": "Issue reappeared" }`
    * Response: updated `IncidentResponse` (status → `REOPENED` or `OPEN` per policy)
* **Audit logs**
    * `GET /api/ims/incidents/{incidentId}/audit-logs`

### Behavior & validations
- **Authorization enforced**: `403 Forbidden` for unauthorized access.
- **Missing resources**: `404 Not Found`.
- **Invalid transitions**: `409 Conflict`, includes current status in response.
- **Required fields validated**: `400 Bad Request` for missing fields.
- **Audit logs**: Created in the exact same transaction as the status change.
- **Idempotent semantics**: Repeated identical assign/resolve requests do not duplicate state changes.

---

## Testing & debugging (concise)

### Run tests
* **All tests:**
  ```bash
  ./gradlew test
  ```
* **Single test class:**
  ```bash
  ./gradlew test --tests "com.repifuzz.service.IncidentServiceTest" --stacktrace
  ```
* **Single test method:**
  ```bash
  ./gradlew test --tests "com.repifuzz.service.IncidentServiceTest.createIncident_persistsAndMapsResponse" --stacktrace
  ```

### Common test issues & fixes
* **Mockito UnnecessaryStubbingException:** Remove unused stubs or mark the specific stub lenient:
  ```java
  org.mockito.Mockito.lenient().when(...).thenReturn(...)
  ```
* **Security tests:** Set an authenticated token in `SecurityContext`:
  ```java
  SecurityContextHolder.getContext().setAuthentication(
    new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList())
  );
  ```
* **JwtUtil:** Ensure test secret length ≥ 32 chars and set `jwt.expirationMs` in `src/test/resources/application-test.properties`.

### Test organization notes
- **Unit tests:** Use `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`
- **Controller tests:** `@SpringBootTest` + `@AutoConfigureMockMvc` with `@MockBean` for collaborators
- **Data tests:** `@DataJpaTest` for JPA/constraint validation

---

## Configuration (test defaults)
File: `src/test/resources/application-test.properties`
```properties
jwt.secret=01234567890123456789012345678901
jwt.expirationMs=60000
```
* Use a sufficiently long JWT secret for tests (≥32 chars).

---

## Documentation & supporting files (recommended)
- `docs/openapi.yaml` — OpenAPI spec for endpoints
- `TESTING.md` — Extended test-debugging recipes
- `CONTRIBUTING.md` — Development workflow, PR, and testing expectations
- `ARCHITECTURE.md` — Entity relationships and sequence diagrams
- `CHANGELOG.md` — Track releases and breaking changes

---

## Contributing
- Fork → branch → PR
- Run tests locally and include test coverage for new behavior
- Keep commits focused and reference issues in PRs

---

## Maintainers & support
- **Maintainer:** Ashish ([pariharashish](https://github.com))
- **Support:** For bugs or feature requests, please open an issue.

---

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.