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
- Node.js 24 (LTS) with npm 11+
- Docker and Docker Compose (optional, for database via container)
- PostgreSQL 18 (if not using Docker)

### Quick Start

#### Option 1: Using Docker Compose (Recommended)

Start PostgreSQL with Docker Compose:

```bash
docker compose up -d postgres
```

The database will be available at `localhost:5432` with the credentials from
`.env` or the defaults in `docker-compose.yml`.

#### Option 2: Local PostgreSQL Installation

Install PostgreSQL 18 and create the database:

```sql
CREATE DATABASE resourcehub;
CREATE USER resourcehub_user WITH PASSWORD 'resourcehub_pass';
GRANT ALL PRIVILEGES ON DATABASE resourcehub TO resourcehub_user;
```

#### Backend

```bash
cd backend
./mvnw spring-boot:run
```

The Maven wrapper downloads the project's pinned Maven version automatically;
a separate Maven installation is not required.

#### Frontend

```bash
cd frontend
npm ci --include=dev
npm run dev
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

## Environment Variables

### Database (PostgreSQL)

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_DB` | Database name | `resourcehub` |
| `POSTGRES_USER` | Database user | `resourcehub_user` |
| `POSTGRES_PASSWORD` | Database password | `resourcehub_pass` |
| `POSTGRES_PORT` | Database port | `5432` |

### Backend Application

The backend uses Spring Boot configuration. See `backend/README.md` for details.

## Development Guidelines

- Backend follows layered Spring Boot architecture: controller → service → repository → database
- Frontend uses React with modern patterns
- All documentation is maintained in the `docs/` directory

## License

[Add license information here]
