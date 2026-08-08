# ResourceHub Backend

Spring Boot backend application for ResourceHub booking and lending system.

## Tech Stack

- Java 25 (LTS)
- Spring Boot 4.1
- Maven 3.9+
- PostgreSQL 18
- Flyway for database migrations

## Development

### Prerequisites

- Java 25 (LTS)
- Maven 3.9+
- PostgreSQL 18

### Database Setup

Create a PostgreSQL database for development:

```sql
CREATE DATABASE resourcehub;
CREATE USER resourcehub_user WITH PASSWORD 'resourcehub_pass';
GRANT ALL PRIVILEGES ON DATABASE resourcehub TO resourcehub_user;
```

### Build

```bash
./mvnw clean install -DskipTests
```

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
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | DDL mode | `update` |
| `SERVER_PORT` | Application port | `8080` |

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

## Build without Tests

For CI/development builds without test execution:

```bash
./mvnw clean install -DskipTests
```