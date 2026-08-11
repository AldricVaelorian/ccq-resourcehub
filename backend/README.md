# ResourceHub Backend

Spring Boot backend application for ResourceHub booking and lending system.

## Tech Stack

- Java 25 (LTS)
- Spring Boot 4.1
- Maven 3.9.16 (downloaded automatically by the Maven wrapper)
- PostgreSQL 18
- Flyway for database migrations

## Development

### Prerequisites

- Java 25 (LTS)
- PostgreSQL 18 (running locally or via Docker Compose)
- Docker and Docker Compose (optional, for database via container)

### Database Setup

#### Option 1: Using Docker Compose (Recommended)

Start PostgreSQL with Docker Compose:

```bash
docker compose up -d postgres
```

The database will be available at `localhost:5432` with default credentials:
- User: `resourcehub_user`
- Password: `resourcehub_pass`
- Database: `resourcehub`

#### Option 2: Local PostgreSQL Installation

Create a PostgreSQL database for development:

```sql
CREATE USER resourcehub_user WITH PASSWORD 'resourcehub_pass';
CREATE DATABASE resourcehub OWNER resourcehub_user;
```

### Compile production sources

```bash
./mvnw -Dmaven.test.skip=true compile
```

The Maven wrapper downloads Maven 3.9.16 on first use, so no system-wide
Maven installation is required.

### Run

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### Docker Compose

For local development with a containerized PostgreSQL database:

```bash
# Start PostgreSQL
docker compose up -d postgres

# Wait for database to be ready (about 10-15 seconds)

# Run the backend application
./mvnw spring-boot:run

# Stop services when done (keeps database data)
docker compose stop

# Restart services after stop
docker compose start

# Stop and remove containers, networks, and volumes (deletes database data)
docker compose down

# To stop and remove volumes explicitly (database data)
docker compose down -v
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/resourcehub` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `resourcehub_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `resourcehub_pass` |
| `SERVER_PORT` | Application port | `8080` |

The default JPA schema mode is `validate`; schema changes must be supplied as
Flyway migrations rather than generated automatically.

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── de/ccq/resourcehub/
│   │   │       ├── controller/     # REST controllers
│   │   │       ├── service/        # Business logic
│   │   │       ├── repository/     # Data access layer
│   │   │       ├── entity/         # JPA entities
│   │   │       ├── dto/            # Data transfer objects
│   │   │       └── exception/      # Exception handlers
│   │   └── resources/
│   │       ├── application.yml     # Main configuration
│   │       └── db/migration/       # Flyway migrations
│   └── test/
│       └── java/
│           └── de/ccq/resourcehub/
└── pom.xml
```

## API Documentation

Once the application is running, visit:
- OpenAPI/Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testing

Run tests (handled by Test Agent):

```bash
./mvnw test
```

## Package without compiling or running tests

For CI/development builds without test execution:

```bash
./mvnw -Dmaven.test.skip=true package
```
