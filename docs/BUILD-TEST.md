# Build & Test Guide

This document describes how to build and test the ResourceHub application.

## Backend

### Building

```bash
cd backend
./mvnw clean package
```

This creates:
- `target/resourcehub-0.0.1-SNAPSHOT.jar` - Executable JAR
- `target/coverage-reports/` - Test coverage reports

### Running Tests

```bash
cd backend
./mvnw test
```

### Running with Coverage

```bash
cd backend
./mvnw clean test jacoco:report
```

### Starting Backend

```bash
cd backend
./mvnw spring-boot:run
```

Or run the JAR directly:
```bash
java -jar target/resourcehub-0.0.1-SNAPSHOT.jar
```

## Frontend

### Building

```bash
cd frontend
npm run build
```

This creates a `build/` directory with optimized production files.

### Running Tests

```bash
cd frontend
npm test
```

### Starting Development Server

```bash
cd frontend
npm start
```

### Running Linter

```bash
cd frontend
npm run lint
```

## Integration Testing

### Start Both Services

**Terminal 1 - Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm start
```

### Smoke Test

1. Backend health check:
   ```bash
   curl http://localhost:8080
   ```

2. Frontend availability:
   - Open `http://localhost:3000` in browser

## CI/CD Notes

- This document covers local development only
- CI/CD pipeline configuration is out of scope for RH-001