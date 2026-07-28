# Authentication Approach

Sprint 6 uses Supabase Auth for user sign-up, sign-in, and session management.
The Spring Boot API validates Supabase-issued JWTs and uses the authenticated
subject as the owner identity for monitors, check results, and incidents.

## Decision

- Frontend auth provider: Supabase Auth
- Backend auth model: JWT resource server
- Ownership model: store the Supabase user id as an external auth identity
- Secret handling: use environment variables only
- First protected domain: monitors

## Why This Approach

- It avoids building password storage and account recovery ourselves.
- It reuses a service already planned for the project database.
- It demonstrates production-style authentication in a portfolio project.
- It keeps the backend responsible for authorization and data ownership.

## Expected Request Flow

```text
User signs in with Supabase Auth
Frontend receives a Supabase access token
Frontend sends Authorization: Bearer <token> to Spring Boot
Spring Boot validates the JWT
Spring Boot reads the authenticated user id from the token subject
API queries and writes data only for that owner identity
```

## Backend Implementation Plan

- Add Spring Security OAuth2 resource server support. Completed.
- Protect monitor APIs. Completed.
- Create an external auth identity model for the Supabase user id. Completed.
- Filter monitors by authenticated owner. Completed.
- Configure JWT issuer/JWK settings through environment variables. Completed.
- Extend related check result and incident access through monitor ownership. Completed.

## Protected Endpoints

Authenticated user data APIs now require a bearer token:

```text
/api/check-results/**
/api/dashboard/**
/api/incidents/**
/api/monitors/**
```

Public local/system endpoints remain available without auth:

```text
/api/health
/actuator/health
/actuator/info
/h2-console/**
```

## Frontend Implementation Plan

- Add Supabase client configuration through Vite environment variables. Completed.
- Add login and register screens/actions. Completed.
- Add logout action. Completed.
- Store the active Supabase session through Supabase client helpers. Completed.
- Attach the access token to API requests. Completed.
- Hide the dashboard until the user is authenticated. Completed.

## Environment Variables

Backend:

```text
DATABASE_URL=
DATABASE_USERNAME=
DATABASE_PASSWORD=
SUPABASE_JWT_ISSUER=
SUPABASE_JWT_JWK_SET_URI=
```

Frontend:

```text
VITE_SUPABASE_URL=
VITE_SUPABASE_ANON_KEY=
```

No Supabase service role key should be used in the frontend.
