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
- Configure JWT issuer/JWK settings through environment variables.
- Create an external auth identity model for the Supabase user id.
- Filter monitors by authenticated owner.
- Extend related check result and incident access through monitor ownership.

## Protected Endpoints

The monitor API now requires an authenticated bearer token:

```text
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

- Add Supabase client configuration through Vite environment variables.
- Add login, register, and logout screens/actions.
- Store the active Supabase session through Supabase client helpers.
- Attach the access token to API requests.
- Hide the dashboard until the user is authenticated.

## Environment Variables

Backend:

```text
SUPABASE_JWT_ISSUER=
SUPABASE_JWT_JWK_SET_URI=
```

Frontend:

```text
VITE_SUPABASE_URL=
VITE_SUPABASE_ANON_KEY=
```

No Supabase service role key should be used in the frontend.
