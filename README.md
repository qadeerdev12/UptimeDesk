# UptimeDesk

UptimeDesk is a full-stack API monitoring dashboard for tracking the health, uptime, latency, and incident history of deployed projects.

I am building this as a practical portfolio project and as a tool I can use to monitor my own applications.

## What It Does

UptimeDesk lets a developer register API endpoints and monitor whether those services are healthy over time.

Example use cases:

- Monitor a portfolio website
- Monitor a Spring Boot backend
- Monitor an ASP.NET API
- Track health-check endpoints for deployed apps
- View uptime and latency history from one dashboard
- Detect outages before users report them

## Core Features

Current foundation:

- Create API monitors
- List monitored services
- Run health checks manually
- Run scheduled background checks
- Store check results
- Track current monitor status
- Show a starter monitoring dashboard

Planned features:

- Edit and delete monitors
- Real uptime analytics
- Latency charts from stored check results
- Incident tracking
- Email alerts
- Authentication
- Supabase PostgreSQL deployment
- Public status pages

Detailed sprint tracking lives in [PROJECT_PLAN.md](./PROJECT_PLAN.md).

## Tech Stack

Frontend:

- React
- Vite
- TypeScript
- Tailwind CSS
- TanStack Query
- Recharts

Backend:

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Spring Scheduler
- Spring Validation

Database and deployment plan:

- Local database: H2
- Production database: Supabase PostgreSQL
- Frontend deployment: Vercel
- Backend deployment: Render

## Project Structure

```text
UptimeDesk/
  client/   React frontend
  server/   Spring Boot backend
```

## How It Works

```text
User creates a monitor
Spring Boot stores the monitor
Scheduler checks the endpoint on an interval
Each check result is saved
Dashboard displays service health, latency, and failures
Incidents and alerts will be added as the project grows
```

## Run Locally

Start the backend:

```bash
cd server
./mvnw spring-boot:run
```

The backend runs at:

```text
http://localhost:8080
```

Start the frontend:

```bash
cd client
npm install
npm run dev
```

The frontend runs at:

```text
http://localhost:5173
```

## Useful API Endpoints

```text
GET  /api/health
GET  /api/monitors
POST /api/monitors
POST /api/monitors/{id}/run
GET  /api/monitors/{id}/results
```

Example create-monitor request:

```bash
curl -X POST http://localhost:8080/api/monitors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Portfolio API",
    "url": "http://localhost:8080/api/health",
    "method": "GET",
    "expectedStatusCode": 200,
    "intervalMinutes": 1,
    "timeoutSeconds": 5
  }'
```

## Development Status

This project is actively being built.

Completed:

- Backend scaffold
- Frontend scaffold
- `client/` and `server/` monorepo structure
- Monitor model
- Check result model
- Scheduled health checking
- Manual health checking
- Starter dashboard UI

Next focus:

- Complete monitor CRUD
- Improve frontend monitor management
- Add real dashboard metrics
- Add Supabase PostgreSQL configuration

## Why This Project

UptimeDesk is designed to show practical full-stack engineering skills:

- REST API design
- scheduled backend jobs
- database modeling
- frontend dashboard UI
- deployment planning
- real-world product thinking

It is intentionally more than a CRUD app: the backend performs scheduled work, records historical results, and will eventually detect incidents and send alerts.
