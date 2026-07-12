# Setup Guide

This document describes how to set up the ResourceHub development environment from scratch.

## Prerequisites

- **Java 17+** for backend (required)
- **Node.js 18+** for frontend
- **Maven 3.6+** (optional, wrapper included)

### Installing Java (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-17-jdk
java -version  # Verify installation - should show openjdk version "17.x"
```

### Installing Java (macOS with Homebrew)
```bash
brew install openjdk@17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### Installing Node.js
Download from [nodejs.org](https://nodejs.org/) or use nvm:
```bash
nvm install 18
nvm use 18
```

## Local Development Setup

### Prerequisites Check

```bash
# Check Java version (required: 17+)
java -version

# Check Node.js version (required: 18+)
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
- Ubuntu: `sudo apt install openjdk-17-jdk`
- macOS: `brew install openjdk@17`

## IDE Setup

### IntelliJ IDEA / Eclipse
- Import backend as Maven project
- Enable annotation processing

### VS Code
- Install Java Extension Pack
- Install ESLint and Prettier extensions for frontend