# Setup Guide

This document describes how to set up the ResourceHub development environment from scratch.

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

### Installing Java (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-25-jdk
java -version  # Verify installation - should show openjdk version "25.x"
```

### Installing Java (macOS with Homebrew)
```bash
brew install openjdk@25
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
```

### Installing Node.js
Use nvm to install Node.js 24 LTS:
```bash
nvm install 24
nvm use 24
```

### Installing Maven (optional, wrapper included)
```bash
# Ubuntu/Debian
sudo apt install maven

# macOS with Homebrew
brew install maven

# Verify version (should be 3.9+)
mvn --version
```

## Local Development Setup

### Prerequisites Check

```bash
# Check Java version (required: 25 LTS)
java -version

# Check Node.js version (required: 24 LTS)
node --version

# Check npm version
npm --version

# Check Maven version (optional, wrapper is included)
mvn --version
```

### Initial Setup

1. Clone the repository (or navigate to the workspace directory)
   ```bash
   cd /home/node/.openclaw/workspace/ccq-resourcehub
   ```

2. Install backend dependencies (handled by Maven wrapper)
   ```bash
   cd backend
   ./mvnw dependency:resolve
   ```

3. Install frontend dependencies
   ```bash
   cd ../frontend
   npm install
   ```

## Starting Development Servers

### Backend (Terminal 1)
```bash
cd backend
./mvnw spring-boot:run
```
Backend runs on: `http://localhost:8080`

### Frontend (Terminal 2)
```bash
cd frontend
npm start
```
Frontend runs on: `http://localhost:3000`

## Build for Production

### Backend
```bash
cd backend
./mvnw clean package
```
Output: `target/resourcehub-0.0.1-SNAPSHOT.jar`

### Frontend
```bash
cd frontend
npm run build
```
Output: `build/` directory

## Common Issues

### Port Already in Use

- Backend: Change `server.port` in `backend/src/main/resources/application.properties`
- Frontend: Frontend will prompt to use a different port

### Node Modules Issues
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

### Backend Dependencies Issues
```bash
cd backend
./mvnw clean install -U
```

### Java Not Found

If you see "JAVA_HOME is not defined", install Java first:
- Ubuntu: `sudo apt install openjdk-25-jdk`
- macOS: `brew install openjdk@25`

## IDE Setup

### IntelliJ IDEA / Eclipse
- Import backend as Maven project
- Enable annotation processing

### VS Code
- Install Java Extension Pack
- Install ESLint and Prettier extensions for frontend