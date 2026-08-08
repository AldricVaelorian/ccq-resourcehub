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
- PostgreSQL 18

### Database Setup

Create a PostgreSQL database for development:

```sql
CREATE DATABASE resourcehub;
CREATE USER resourcehub_user WITH PASSWORD 'resourcehub_pass';
GRANT ALL PRIVILEGES ON DATABASE resourcehub TO resourcehub_user;
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
