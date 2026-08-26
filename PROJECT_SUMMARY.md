# Project Summary: Repifuzz Assignment

## Overview

Repifuzz Assignment is a single Spring Boot REST API for basic incident management. It lets users register and log in, then provides endpoints to create and retrieve incidents. The application persists its data in MySQL and includes JWT token generation and request-filtering infrastructure.

## High-Level Architecture

```text
Client
  |
  v
Spring MVC Controllers
  |
  v
Service Layer
  |
  v
Spring Data JPA Repositories
  |
  v
MySQL (repifuzz_db)

JWT Authentication Filter -> Spring Security Context
```

The project uses a conventional layered architecture:

- **Controllers** expose HTTP endpoints and delegate work to services.
- **Services** contain business logic, including password hashing, authentication support, incident ID generation, persistence coordination, and response mapping.
- **Repositories** are Spring Data JPA interfaces that query and persist entities.
- **Entities** map Java objects to database tables.
- **Security components** generate/validate JWTs and configure Spring Security as stateless.

## Packages and Important Classes

| Package | Class | Responsibility |
|---|---|---|
| `com.repifuzz` | `RepifuzzAssignmnetApplication` | Application entry point; starts Spring Boot and component scanning. |
| `controller` | `AuthController` | Registration and login endpoints. |
| `controller` | `IncidentController` | Incident creation and lookup endpoints. |
| `service` | `UserService` | Hashes passwords with BCrypt and saves users. |
| `service` | `CustomUserDetailsService` | Loads users by email for Spring Security authentication. |
| `service` | `IncidentService` | Creates/retrieves incidents, creates unique business IDs, and maps responses. |
| `Repo` | `UserRepository` | JPA data access for `User` entities, including email/username lookup. |
| `Repo` | `IncidentRepository` | JPA data access for incidents and incident ID lookups. |
| `Entity` | `User` | Maps users to the `users` table. |
| `Entity` | `Incident` | Maps incidents to the `incidents` table. |
| `Entity` | `IncidentType` | Allowed incident categories: `ENTERPRISE` and `GOVERNMENT`. |
| `EntityDTO` | `LoginRequest` | Login JSON request payload. |
| `EntityDTO` | `IncidentRequest` | Incident creation JSON request payload. |
| `EntityDTO` | `IncidentResponse` | Incident API response payload. |
| `jwtUtil` | `JwtUtil` | Creates, parses, and validates signed JWTs. |
| `securityConfig` | `JwtAuthenticationFilter` | Reads Bearer tokens and establishes the SecurityContext when valid. |
| `securityConfig` | `SecurityConfig` | Configures BCrypt, stateless security, the authentication manager, and the JWT filter. |

## REST APIs

Base path: `/api/ims`

| Method | Endpoint | Purpose | Request body / parameter | Response |
|---|---|---|---|---|
| `POST` | `/user/register` | Creates a user account. | `username`, `email`, optional profile/contact fields, `password` | Persisted user object. |
| `POST` | `/user/login` | Authenticates by email and password. | `email`, `password` | JSON containing a JWT token. |
| `POST` | `/incidents` | Creates an incident. | `reporterUserId`, reporter details, `incidentType`, `description`, `details` | Created `IncidentResponse`. |
| `GET` | `/incidents/{incidentId}` | Retrieves one incident by business incident ID. | Path variable `incidentId` | `IncidentResponse`. |

### Authentication behavior

Login returns a JWT with the user's email as its subject and a configured 24-hour lifetime. The `JwtAuthenticationFilter` recognizes an `Authorization: Bearer <token>` header and loads the corresponding user into Spring Security's context.

`POST /api/ims/user/register` and `POST /api/ims/user/login` are public. Every other route, including incident creation and lookup, requires a valid JWT. Requests to protected routes without valid authentication receive `401 Unauthorized`.

## Database

The configured datasource is a local MySQL instance:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/repifuzz_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
spring.jpa.hibernate.ddl-auto=update
```

Hibernate uses `ddl-auto=update`, so it creates or updates the mapped schema at startup.

### `users` table

Mapped by `User`.

| Column | Notes |
|---|---|
| `id` | Auto-increment primary key. |
| `username` | Required and unique. |
| `email` | Required and unique; used for login and JWT subject. |
| `phone`, `address`, `pin_code`, `city`, `country` | User profile/contact data. |
| `password` | BCrypt-hashed password. |

### `incidents` table

Mapped by `Incident`.

| Column | Notes |
|---|---|
| `id` | Auto-increment primary key. |
| `incident_id` | Required unique business ID. Generated as `RMG` + five digits + current year, for example `RMG012342026`. |
| `reporter_user_id` | Foreign key to `users.id`. |
| `reporter_name`, `reporter_email`, `reporter_phone` | Reporter details stored with the incident. |
| `incident_type` | String enum: `ENTERPRISE` or `GOVERNMENT`. |
| `description` | Large text field. |
| `details` | MySQL JSON column stored as a Java `String`. |
| `created_at`, `updated_at` | Managed by JPA lifecycle callbacks. |

## Third-Party Integrations and Dependencies

There are no outbound SaaS, HTTP API, message-broker, or payment integrations in the current codebase.

Key dependencies are:

- Spring Boot Web: REST API and embedded server.
- Spring Data JPA / Hibernate: ORM and data persistence.
- MySQL Connector/J: MySQL driver.
- Spring Security: authentication infrastructure and request filtering.
- JJWT 0.13.0: JWT creation and verification.
- Lombok: generated getters, setters, constructors, and builders.
- Spring Boot DevTools: development-time restart support.

## Build and Run

### Prerequisites

- Java 17
- A local MySQL server
- A MySQL database named `repifuzz_db`
- Environment variables for the database and JWT secret: `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET`

The repository includes `.env.example` and `src/main/resources/application-example.properties` as non-secret reference files. Never commit the real values. The `.env` and `application-local.properties` files are ignored by Git.

### Commands

On Windows PowerShell:

```powershell
$env:DB_USERNAME = "your_mysql_username"
$env:DB_PASSWORD = "your_mysql_password"
$env:JWT_SECRET = "replace_with_a_random_secret_of_at_least_32_bytes"
.\gradlew.bat build
.\gradlew.bat test
.\gradlew.bat bootRun
```

On macOS/Linux:

```bash
export DB_USERNAME="your_mysql_username"
export DB_PASSWORD="your_mysql_password"
export JWT_SECRET="replace_with_a_random_secret_of_at_least_32_bytes"
./gradlew build
./gradlew test
./gradlew bootRun
```

The project uses the Gradle Wrapper (Gradle 8.14.3) and Java 17. `build` compiles source, processes resources, runs tests, and produces the executable Spring Boot artifact in `build/libs/`. With no explicit `server.port` setting, the application runs on port `8080` by default.

## Request Flow Examples

### Login

```text
POST /api/ims/user/login
  -> AuthenticationManager
  -> CustomUserDetailsService loads user by email
  -> BCrypt verifies password
  -> JwtUtil signs a token
  -> { "token": "..." }
```

### Create an incident

```text
POST /api/ims/incidents
  -> JwtAuthenticationFilter optionally processes Bearer token
  -> IncidentController
  -> IncidentService finds reporter user
  -> IncidentService generates unique incident ID
  -> IncidentRepository saves entity
  -> IncidentResponse returned
```

## Testing

The project contains one Spring Boot context-loading test class, `RepifuzzAssignmnetApplicationTests`. No dedicated unit, controller, repository, or integration tests are currently present.
