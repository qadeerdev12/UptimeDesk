# UptimeDesk

UptimeDesk is a full-stack API monitoring dashboard for tracking the health, uptime, latency, and incident history of deployed projects.

The goal is to build a portfolio-quality monitoring tool that can also be used for real projects, including personal websites, Spring Boot APIs, ASP.NET APIs, and health-check endpoints.

## Project Structure

```text
UptimeDesk/
  client/   React + Vite + TypeScript frontend
  server/   Spring Boot backend
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
- Spring Validation
- Spring Scheduler
- H2 for local development
- PostgreSQL for hosted/deployed database

Database plan:

- Local development: H2 first, then optional local PostgreSQL
- Hosted portfolio database: Supabase PostgreSQL

Deployment plan:

- Frontend: Vercel
- Backend: Render
- Database: Supabase PostgreSQL

## Current Status

- [x] Spring Boot backend scaffolded
- [x] React frontend scaffolded
- [x] Repo restructured into `client/` and `server/`
- [x] Basic monitor entity created
- [x] Basic check-result entity created
- [x] Manual endpoint checking added
- [x] Scheduled checking added
- [x] Starter dashboard UI added
- [x] Frontend connected to `/api/monitors`
- [x] Backend tests passing
- [x] Frontend build passing
- [x] Frontend lint passing

## Run Locally

Backend:

```bash
cd server
./mvnw spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

Frontend:

```bash
cd client
npm install
npm run dev
```

Frontend runs on:

```text
http://localhost:5173
```

## Product Vision

UptimeDesk helps developers monitor their deployed projects from one dashboard.

Core user flow:

```text
User adds an API endpoint
UptimeDesk checks it on a schedule
Each check result is stored
Dashboard shows status, uptime, latency, and failures
Repeated failures create incidents
User receives alerts when something goes down
```

## Sprint Roadmap

### Sprint 1: Project Foundation

Goal: Set up the full-stack project and prove the backend/frontend can run.

- [x] Create Spring Boot backend
- [x] Create React frontend
- [x] Restructure repo into `client/` and `server/`
- [x] Add README roadmap
- [x] Verify backend test command
- [x] Verify frontend build command
- [x] Verify frontend lint command

Definition of done:

- [x] Backend starts locally
- [x] Frontend starts locally
- [x] Repo structure is easy to understand
- [x] README explains how to run both apps

### Sprint 2: Monitor Management

Goal: Let users create, view, update, and delete monitored endpoints.

- [x] Create `Monitor` entity
- [x] Create monitor API endpoint
- [x] List monitors API endpoint
- [ ] Get single monitor by ID
- [ ] Update monitor
- [ ] Delete monitor
- [ ] Add backend request/response DTOs for monitor APIs
- [ ] Add validation for URL format
- [ ] Add frontend monitor list page
- [ ] Add frontend create-monitor form
- [ ] Add frontend edit-monitor flow
- [ ] Add loading, empty, and error states

Definition of done:

- [ ] User can manage monitors from the UI
- [ ] Invalid monitor data is rejected clearly
- [ ] API responses do not expose unnecessary JPA internals

### Sprint 3: Health Checks and Results

Goal: Make UptimeDesk actually check endpoints and store useful history.

- [x] Create `CheckResult` entity
- [x] Add manual `run check now` endpoint
- [x] Add scheduled checker
- [x] Store status code, response time, status, timestamp, and error message
- [x] Update monitor status after checks
- [ ] Add retry-before-failure logic
- [ ] Add support for custom timeout per monitor
- [ ] Add support for expected response keyword
- [ ] Add support for request headers
- [ ] Add check result detail endpoint
- [ ] Add frontend recent-results table
- [ ] Add frontend latency chart from real check data

Definition of done:

- [ ] A monitor can be checked manually and automatically
- [ ] Recent checks are visible in the UI
- [ ] Failed checks show a useful reason

### Sprint 4: Dashboard Analytics

Goal: Make the dashboard useful at a glance.

- [ ] Calculate uptime percentage for 24h, 7d, and 30d
- [ ] Calculate average latency
- [ ] Show number of active monitors
- [ ] Show number of down monitors
- [ ] Show latest failed checks
- [ ] Add dashboard summary endpoint
- [ ] Replace sample frontend metrics with real backend data
- [ ] Add dashboard chart filters
- [ ] Add monitor status pills: Operational, Degraded, Down, Unknown

Definition of done:

- [ ] Dashboard gives a clear answer to: "Are my projects healthy?"
- [ ] Metrics come from real backend data
- [ ] UI remains readable on laptop and mobile widths

### Sprint 5: Incidents

Goal: Track outages as incidents instead of isolated failed checks.

- [ ] Create `Incident` entity
- [ ] Open incident after repeated failures
- [ ] Keep incident open while monitor is down
- [ ] Resolve incident when monitor recovers
- [ ] Add acknowledge incident action
- [ ] Add incident history endpoint
- [ ] Add active incidents panel
- [ ] Add incident detail page
- [ ] Add incident timeline events

Definition of done:

- [ ] Repeated failures create an incident
- [ ] Recovery resolves the incident
- [ ] User can see what happened and when

### Sprint 6: Authentication and Ownership

Goal: Add real user accounts so monitors belong to the correct user.

Preferred auth direction:

- Spring Security + JWT for backend-owned auth, or
- Supabase Auth integration if we want to reuse Supabase as auth provider

Tasks:

- [ ] Decide auth approach
- [ ] Add `User` or external auth identity model
- [ ] Protect monitor APIs
- [ ] Associate monitors with authenticated users
- [ ] Add login/register UI
- [ ] Add logout
- [ ] Add route protection on frontend
- [ ] Hide data between users

Definition of done:

- [ ] Users can only access their own monitors
- [ ] Auth flow works from the frontend
- [ ] Tokens/secrets are handled through environment variables

### Sprint 7: Supabase PostgreSQL

Goal: Move from local H2 to hosted PostgreSQL for portfolio deployment.

- [ ] Create Supabase project or reuse existing Supabase account
- [ ] Create UptimeDesk PostgreSQL database credentials
- [ ] Add `application-dev.properties`
- [ ] Add `application-prod.properties`
- [ ] Configure datasource using environment variables
- [ ] Test backend against Supabase PostgreSQL
- [ ] Add database migration strategy with Flyway or Liquibase
- [ ] Replace `ddl-auto=update` for production

Definition of done:

- [ ] Backend can run against Supabase PostgreSQL
- [ ] No database secrets are committed
- [ ] Production schema is managed safely

### Sprint 8: Alerts

Goal: Notify the user when something goes down.

- [ ] Create alert channel model
- [ ] Add email alert configuration
- [ ] Send alert when incident opens
- [ ] Send recovery email when incident resolves
- [ ] Add alert cooldown to avoid spam
- [ ] Add frontend alert settings page
- [ ] Optional: Discord webhook alerts
- [ ] Optional: Slack webhook alerts

Definition of done:

- [ ] User receives an alert for a real outage
- [ ] User receives a recovery notification
- [ ] Repeated failures do not spam the user

### Sprint 9: Public Status Page

Goal: Let users share a public status page for selected monitors.

- [ ] Add public/private monitor flag
- [ ] Add status page slug
- [ ] Add public status API endpoint
- [ ] Add public status frontend route
- [ ] Show current status for public monitors
- [ ] Show recent incidents
- [ ] Show uptime summary

Definition of done:

- [ ] A visitor can view a public status page without logging in
- [ ] Private monitors remain hidden

### Sprint 10: Portfolio Polish and Deployment

Goal: Make the project presentable for recruiters and interviews.

- [ ] Add seed/demo data
- [ ] Add screenshots to README
- [ ] Add architecture diagram
- [ ] Add API documentation
- [ ] Add backend unit tests for monitor checking
- [ ] Add backend controller tests
- [ ] Add frontend component tests or smoke tests
- [ ] Add Dockerfile for backend
- [ ] Add Dockerfile for frontend or static deployment instructions
- [ ] Deploy backend to Render
- [ ] Deploy frontend to Vercel
- [ ] Connect deployed backend to Supabase PostgreSQL
- [ ] Configure production frontend API URL
- [ ] Configure backend CORS for Vercel domain
- [ ] Configure Render environment variables
- [ ] Add live demo link
- [ ] Add short demo video

Definition of done:

- [ ] Live demo works
- [ ] Vercel frontend can call Render backend
- [ ] Render backend can connect to Supabase PostgreSQL
- [ ] README tells the project story clearly
- [ ] Codebase shows clean junior/graduate-level engineering habits

## Feature List

### MVP Features

- [x] Create monitors
- [x] List monitors
- [x] Run checks manually
- [x] Run checks on a schedule
- [x] Store check results
- [x] Basic dashboard UI
- [ ] Edit monitors
- [ ] Delete monitors
- [ ] Real dashboard metrics
- [ ] Real latency chart
- [ ] Recent check history UI

### Intermediate Features

- [ ] Incidents
- [ ] Email alerts
- [ ] Authentication
- [ ] Supabase PostgreSQL connection
- [ ] User-owned monitors
- [ ] Public status page
- [ ] Uptime analytics
- [ ] Request headers
- [ ] Keyword checks
- [ ] Retry rules

### Advanced Features

- [ ] Team accounts
- [ ] Role-based access
- [ ] Slack/Discord alerts
- [ ] SSL certificate expiry checks
- [ ] Custom status page branding
- [ ] Multi-region checks
- [ ] Export uptime reports
- [ ] Webhook notifications

## Useful API Endpoints

Create a monitor:

```bash
curl -X POST http://localhost:8080/api/monitors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "UptimeDesk Health",
    "url": "http://localhost:8080/api/health",
    "method": "GET",
    "expectedStatusCode": 200,
    "intervalMinutes": 1,
    "timeoutSeconds": 5
  }'
```

List monitors:

```bash
curl http://localhost:8080/api/monitors
```

Run a check immediately:

```bash
curl -X POST http://localhost:8080/api/monitors/1/run
```

View recent results:

```bash
curl http://localhost:8080/api/monitors/1/results
```

## Development Notes

- Keep secrets out of Git.
- Use environment variables for Supabase database credentials.
- Keep `client/` and `server/` separate but in the same repo.
- Prefer small, working increments over large unfinished features.
- Update this README checklist after each completed task.
