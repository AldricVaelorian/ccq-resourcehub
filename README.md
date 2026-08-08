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
- Maven 3.9+
- PostgreSQL 18 (for local development)

### Quick Start

#### Backend

```bash
cd backend
./mvnw spring-boot:run
```

#### Frontend

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