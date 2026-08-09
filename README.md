# ResourceHub - Monorepo

ResourceHub is a booking and lending system for shared resources such as rooms, vehicles, technical devices, and workspaces.

## Project Structure

```
ccq-resourcehub/
├── backend/      # Spring Boot backend application
├── frontend/     # React frontend application
└── docs/         # Project documentation
```

## Development Setup

### Prerequisites

- Java 25 (LTS)
- Node.js 20+ (LTS)
- Docker Compose (for local PostgreSQL via containers)

### Quick Start

#### Option 1: With Docker Compose (Recommended)

Start PostgreSQL with Docker Compose:

```bash
docker-compose up -d postgres
```

Then start the backend and frontend as described below.

#### Option 2: Local Development (without Docker)

##### Backend

```bash
cd backend
./mvnw spring-boot:run
```

The Maven wrapper downloads the project's pinned Maven version automatically;
a separate Maven installation is not required.

##### Frontend

```bash
cd frontend
npm install
npm start
```

## Project Goals

ResourceHub supports:

- User and role management
- Resource and resource category management
- Availability and opening hours
- Single and recurring bookings
- Overlap detection
- Approval workflows for restricted resources
- Maintenance and blocked time periods
- Waitlists and automatic promotion
- Lending and return documentation
- Cancellations
- Simulated notifications
- Audit logging
- Resource usage statistics

## Development Guidelines

- Backend follows layered Spring Boot architecture: controller → service → repository → database
- Frontend uses React with modern patterns
- All documentation is maintained in the `docs/` directory

## License

[Add license information here]
