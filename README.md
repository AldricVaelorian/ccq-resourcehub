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

- Java 17+ for backend
- Node.js 18+ for frontend
- Maven 3.6+ (optional, wrapper included)

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
│   │       ├── static/      # Static resources
│   │       └── templates/   # Template files
│   └── test/
│       └── java/com/example/resourcehub/
```

### Frontend Structure

```
frontend/
├── public/
├── src/
│   ├── App.css
│   ├── App.js
│   ├── index.css
│   └── index.js
├── package.json
└── README.md
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