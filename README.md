# ResourceHub

A monorepo project with Spring Boot backend and React frontend.

## Project Structure

```
ccq-resourcehub/
├── backend/      # Spring Boot backend application
├── frontend/     # React frontend application
└── docs/         # Documentation
```

## Prerequisites

### Backend
- **Java 25 LTS** (required)
- **Spring Boot 4.1**
- **Maven 3.9** (wrapper included)
- **PostgreSQL 18** (for database)

### Frontend
- **Node.js 24 LTS**
- **React 19**
- **TypeScript 6**
- **Vite 8.1**
- **Tailwind CSS 4.3**

### Testing
- **JUnit 5**, **Mockito**, **AssertJ**
- **Spring test slices**, **Testcontainers**

### Database Migrations
- **Flyway** or **Liquibase**

### API Style
- **REST**

## Quick Start

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend

```bash
cd frontend
npm start
```

The frontend will start on `http://localhost:3000`

## Build Commands

### Backend

```bash
cd backend
./mvnw clean package
```

### Frontend

```bash
cd frontend
npm run build
```

## Development

### Backend Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/resourcehub/
│   │   │   └── ResourceHubApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── db/
│   │       │   └── migration/    # Flyway/Liquibase migrations
│   │       └── static/           # Static resources
│   └── test/
│       └── java/com/example/resourcehub/
```

### Frontend Structure

```
frontend/
├── public/
├── src/
│   ├── assets/
│   ├── components/
│   ├── pages/
│   ├── App.tsx
│   ├── main.tsx
│   └── vite-env.d.ts
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── tailwind.config.js
```

## Configuration

Environment variables can be configured via:

- Backend: `backend/src/main/resources/application.properties`
- Frontend: Environment variables in `.env` file (create in frontend directory)

## Testing

### Backend

```bash
cd backend
./mvnw test
```

### Frontend

```bash
cd frontend
npm test
```

## Documentation

See the `docs/` directory for additional documentation.