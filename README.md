# Movie Library API

A secure Spring Boot REST API for managing a movie catalog with automatic rating enrichment from OMDb.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [API Endpoints](#api-endpoints)
- [Authentication](#authentication)
- [Usage Examples](#usage-examples)
- [Database](#database)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Documentation Files](#documentation-files)

---

## Features

- **CRUD Operations** - Create, read, update, delete movies
- **Role-Based Security** - ADMIN (full access) and USER (read-only) roles
- **Async Rating Enrichment** - Background fetching of ratings from OMDb API
- **Input Validation** - Title required, release year between 1888-2100
- **Swagger UI** - Interactive API documentation
- **SQL Scripts** - Explicit database schema (not auto-generated)

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.2 |
| Language | Java 17 |
| Security | Spring Security (HTTP Basic) |
| Database | H2 (in-memory) |
| ORM | Spring Data JPA / Hibernate |
| Documentation | SpringDoc OpenAPI 3.0 |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito, MockMvc |

---

## Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- (Optional) OMDb API key from [omdbapi.com](https://www.omdbapi.com/apikey.aspx)

---

## Quick Start

### 1. Clone and Build

```bash
git clone <repository-url>
cd movie-library
mvn clean package
```

### 2. Run the Application

```bash
# Using default settings (demo API key)
java -jar target/movie-library-1.0.0.jar

# With your OMDb API key
OMDB_API_KEY=your_key java -jar target/movie-library-1.0.0.jar

# Or using Maven
mvn spring-boot:run
```

### 3. Access the API

| Resource | URL |
|----------|-----|
| API Base | http://localhost:8080/api/movies |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |

---

## API Endpoints

| Method | Endpoint | Description | Role Required |
|--------|----------|-------------|---------------|
| GET | /api/movies | List all movies | USER, ADMIN |
| GET | /api/movies/{id} | Get movie by ID | USER, ADMIN |
| POST | /api/movies | Create new movie | ADMIN |
| PUT | /api/movies/{id} | Update movie | ADMIN |
| DELETE | /api/movies/{id} | Delete movie | ADMIN |

---

## Authentication

The API uses HTTP Basic Authentication.

### Test Credentials

| Username | Password | Role | Access |
|----------|----------|------|--------|
| admin | admin123 | ADMIN | Full CRUD |
| user | user123 | USER | Read only |

### HTTP Response Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 204 | Deleted (no content) |
| 400 | Validation error |
| 401 | Unauthorized (no credentials) |
| 403 | Forbidden (insufficient role) |
| 404 | Movie not found |

---

## Usage Examples

### Create Movie (Admin)

```bash
curl -X POST http://localhost:8080/api/movies \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Shawshank Redemption",
    "director": "Frank Darabont",
    "releaseYear": 1994
  }'
```

**Response (201 Created):**
```json
{
  "id": 1,
  "title": "The Shawshank Redemption",
  "director": "Frank Darabont",
  "releaseYear": 1994,
  "rating": null,
  "ratingStatus": "PENDING",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### Get All Movies (User)

```bash
curl http://localhost:8080/api/movies -u user:user123
```

### Get Single Movie

```bash
curl http://localhost:8080/api/movies/1 -u user:user123
```

### Update Movie (Admin)

```bash
curl -X PUT http://localhost:8080/api/movies/1 \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Shawshank Redemption",
    "director": "Frank Darabont",
    "releaseYear": 1994
  }'
```

### Delete Movie (Admin)

```bash
curl -X DELETE http://localhost:8080/api/movies/1 -u admin:admin123
```

### Access Denied Example (User trying to create)

```bash
curl -X POST http://localhost:8080/api/movies \
  -u user:user123 \
  -H "Content-Type: application/json" \
  -d '{"title": "Test"}'
```

**Response (403 Forbidden):**
```json
{
  "status": 403,
  "message": "Access denied. Insufficient permissions.",
  "path": "/api/movies",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Database

### H2 Console Access

1. Navigate to http://localhost:8080/h2-console
2. Enter connection details:
    - **JDBC URL:** `jdbc:h2:mem:moviedb`
    - **Username:** `sa`
    - **Password:** (leave empty)
3. Click Connect

### Schema Files

| File | Purpose |
|------|---------|
| `src/main/resources/schema.sql` | H2 database schema |
| `src/main/resources/data.sql` | Sample data (10 movies) |
| `sql/schema-postgresql.sql` | PostgreSQL production schema |

### Rating Status Values

| Status | Description |
|--------|-------------|
| PENDING | Rating lookup in progress |
| ENRICHED | Rating successfully retrieved |
| NOT_FOUND | Movie not found in OMDb |
| ERROR | API call failed |

---

## API Documentation

### Swagger UI

Interactive documentation at: http://localhost:8080/swagger-ui.html

1. Click "Authorize" button
2. Enter username and password
3. Try out any endpoint

### OpenAPI Spec

JSON specification at: http://localhost:8080/v3/api-docs

Can be imported into Postman or other API tools.

---

## Testing

### Run All Tests

```bash
mvn test
```

### Test Coverage

```bash
mvn test jacoco:report
# Report at: target/site/jacoco/index.html
```

### Test Types

| Class | Type | Tests |
|-------|------|-------|
| MovieControllerTest | Integration | Security, HTTP responses |
| MovieServiceTest | Unit | Business logic |

---

## Project Structure

```
movie-library/
├── pom.xml                     # Maven dependencies
├── README.md                   # This file
├── TECHNICAL_DOCUMENT.md       # Architecture details
├── sql/
│   └── schema-postgresql.sql   # Production DB schema
└── src/
    ├── main/
    │   ├── java/.../
    │   │   ├── config/         # Security, Async, OpenAPI
    │   │   ├── controllers/     # REST endpoints
    │   │   ├── models/dto/            # Request/Response objects
    │   │   ├── models/          # JPA models
    │   │   ├── exceptions/      # Error handling
    │   │   ├── repositories/     # Data access
    │   │   └── services/        # Business logic
    │   └── resources/
    │       ├── application.yml # Configuration
    │       ├── schema.sql      # DB schema
    │       └── data.sql        # Sample data
    └── test/                   # Unit & integration tests
```

---

## Configuration

### application.yml

```yaml
# Server
server:
  port: 8080

# Database
spring:
  datasource:
    url: jdbc:h2:mem:moviedb

# OMDb API
omdb:
  api:
    key: ${OMDB_API_KEY:demo}
    url: http://www.omdbapi.com/
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| OMDB_API_KEY | OMDb API key | demo |
| SERVER_PORT | Server port | 8080 |

---

## Documentation Files

| File | Description |
|------|-------------|
| **README.md** | Quick start guide (this file) |
| **TECHNICAL_DOCUMENT.md** | Architecture, design decisions, trade-offs |

---

## License

MIT License - see LICENSE file for details.