# UptimeDesk

**UptimeDesk is a full-stack API monitoring dashboard for developers who want a simple way to track uptime, latency, and service health across deployed projects.**

It is built as a practical portfolio project and as a real tool for monitoring personal applications, backend APIs, portfolio sites, and health-check endpoints.

## Overview

UptimeDesk lets you register API endpoints, define what a healthy response looks like, run checks manually, and collect scheduled health-check results over time.

The project is intentionally more than a basic CRUD app. The backend performs scheduled work, stores historical check results, and provides the foundation for incidents, alerts, uptime analytics, and public status pages.

## Key Features

Implemented:

- Create, view, update, and delete API monitors
- Configure monitor URL, HTTP method, expected status code, interval, timeout, and active state
- Optionally require a keyword in the response body for deeper health checks
- Optionally send custom request headers with health checks
- Configure how many consecutive failures are required before a monitor is marked down
- Run manual health checks from the dashboard
- Run scheduled backend checks
- Store check results with status code, latency, timestamp, and error details
- Record incident-rule outcomes when checks would open or resolve an incident
- Track active incidents with detail views and timeline events
- Authenticate users with Supabase login, registration, and logout UI
- Protect monitor APIs and scope monitors to the authenticated user
- View monitor status, configuration, and recent check results
- Search monitored services
- Display dashboard metrics and latency chart from collected check results
- Show clear monitor status pills for Operational, Degraded, Down, and Unknown states
- Handle loading, empty, error, and backend-offline states

Planned:

- Email alerts for outages and recoveries
- Supabase PostgreSQL production database
- Public status pages
- Vercel frontend deployment
- Render backend deployment

## Authentication Direction

Sprint 6 uses Supabase Auth on the frontend and Spring Boot JWT validation on the backend. The frontend attaches the active Supabase access token to API requests, and the API uses the authenticated Supabase user id as the ownership key so each user can only access their own monitor records.

## Use Cases

- Monitor a portfolio website
- Monitor a Spring Boot backend
- Monitor an ASP.NET API
- Track health-check endpoints for deployed projects
- Detect downtime before users report it
- Build a central dashboard for personal project reliability

## Architecture

```mermaid
flowchart LR
    User["Developer"] --> Client["React Client<br/>Vercel"]
    Client --> API["Spring Boot API<br/>Render"]
    API --> DB["Supabase PostgreSQL"]
    API --> Scheduler["Spring Scheduler"]
    Scheduler --> Targets["Monitored APIs<br/>Portfolio, apps, services"]
    API --> Results["Check Results<br/>Latency, status, errors"]
    Results --> DB
```

## Tech Stack

Frontend:

- React
- Vite
- TypeScript
- Tailwind CSS
- TanStack Query
- Recharts
- Lucide React

Backend:

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Spring Scheduler
- Spring Validation
- JUnit and MockMvc

Database and deployment:

- Local database: H2
- Production database: Supabase PostgreSQL
- Frontend deployment target: Vercel
- Backend deployment target: Render

## Project Structure

```text
UptimeDesk/
  client/   React + Vite frontend
  server/   Spring Boot backend
```

## How It Works

```text
Create a monitor
Choose URL, method, expected status, interval, and timeout
Spring Boot stores the monitor
Scheduler checks active monitors when they are due
Each result is saved with latency and status details
The dashboard shows monitor health and recent results
```

## Local Setup

Clone the repository:

```bash
git clone https://github.com/<your-username>/UptimeDesk.git
cd UptimeDesk
```

Start the backend:

```bash
cd server
./mvnw spring-boot:run
```

The backend uses the `dev` profile by default, which runs against local H2.

The backend runs at:

```text
http://localhost:8080
```

Start the frontend in a second terminal:

```bash
cd client
npm install
npm run dev
```

The frontend runs at:

```text
http://localhost:5173
```

The Vite dev server proxies `/api` requests to the Spring Boot backend.

## Backend Profiles

Development profile:

```bash
cd server
./mvnw spring-boot:run
```

Production profile:

```bash
cd server
SPRING_PROFILES_ACTIVE=prod \
DATABASE_URL=jdbc:postgresql://your-supabase-host:5432/postgres \
DATABASE_USERNAME=your_database_user \
DATABASE_PASSWORD=your_database_password \
SUPABASE_JWT_ISSUER=your_supabase_jwt_issuer \
SUPABASE_JWT_JWK_SET_URI=your_supabase_jwk_set_uri \
./mvnw spring-boot:run
```

Production values must come from deployment environment variables, not committed files.

## API Endpoints

```text
GET    /api/health
GET    /api/monitors
POST   /api/monitors
GET    /api/monitors/{id}
PUT    /api/monitors/{id}
DELETE /api/monitors/{id}
POST   /api/monitors/{id}/run
GET    /api/monitors/{id}/results
GET    /api/check-results/{id}
GET    /api/dashboard/summary
GET    /api/incidents/active
GET    /api/incidents/{id}
POST   /api/incidents/{id}/acknowledge
GET    /api/monitors/{id}/incidents
```

The monitor, dashboard, check-result, and incident data endpoints are protected by Spring Security, expect a bearer token, and return only records owned by the authenticated user.

Example monitor creation:

```bash
curl -X POST http://localhost:8080/api/monitors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Portfolio API",
    "url": "https://example.com/api/health",
    "method": "GET",
    "expectedStatusCode": 200,
    "intervalMinutes": 5,
    "timeoutSeconds": 5
  }'
```

## Verification

Backend:

```bash
cd server
./mvnw test
```

Frontend:

```bash
cd client
npm run lint
npm run build
```

## Current Status

Completed:

- Monorepo structure with `client/` and `server/`
- Spring Boot API foundation
- React dashboard foundation
- Monitor CRUD backend
- Monitor management frontend
- Manual and scheduled checks
- Check result storage
- Retry-before-failure logic
- Custom request headers for health checks
- Incident-rule foundation for outage and recovery tracking
- Recent results UI and real latency chart data
- Backend-driven dashboard analytics and status pills
- Backend integration tests
- Frontend lint and production build checks

Next milestones:

- Connect production profile to Supabase PostgreSQL
- Add database migrations with Flyway or Liquibase
- Add email alert configuration
- Deploy frontend to Vercel and backend to Render

## Deployment Plan

Frontend on Vercel:

- Root directory: `client`
- Build command: `npm run build`
- Output directory: `dist`

Backend on Render:

- Root directory: `server`
- Build command: `./mvnw clean package`
- Start command: `java -jar target/*.jar`
- Environment: `SPRING_PROFILES_ACTIVE=prod`

Database:

- Supabase PostgreSQL
- Credentials supplied through `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`
- Setup guide: [`docs/supabase-postgres.md`](docs/supabase-postgres.md)
- Alerts guide: [`docs/alerts.md`](docs/alerts.md)
- Production schema managed with Flyway migrations

## Portfolio Value

UptimeDesk demonstrates:

- Full-stack application structure
- REST API design
- Scheduled backend jobs
- Database modeling
- React dashboard development
- API state management
- Validation and error handling
- Testing with Spring Boot and MockMvc
- Realistic deployment planning

This project is designed to show practical engineering judgment, not just framework familiarity.
