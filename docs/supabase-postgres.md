# Supabase PostgreSQL Setup

This guide tracks the database values UptimeDesk needs for the Spring Boot production profile.

## Goal

Use Supabase PostgreSQL as the hosted production database while keeping local development on H2.

## Recommended Connection For Render

Use the Supabase Session Pooler connection for the deployed Spring Boot API.

Why:

- Render-hosted apps commonly need IPv4-compatible database access.
- Supabase Session Pooler is available on every project and uses port `5432`.
- It works well for a persistent Spring Boot backend.

Spring Boot expects a JDBC URL, so convert the Supabase connection string into this shape:

```text
jdbc:postgresql://aws-0-your-region.pooler.supabase.com:5432/postgres?sslmode=require
```

Use these environment variable names:

```text
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://aws-0-your-region.pooler.supabase.com:5432/postgres?sslmode=require
DATABASE_USERNAME=postgres.your-project-ref
DATABASE_PASSWORD=your-database-password
SUPABASE_JWT_ISSUER=https://your-project-ref.supabase.co/auth/v1
SUPABASE_JWT_JWK_SET_URI=https://your-project-ref.supabase.co/auth/v1/.well-known/jwks.json
```

## Supabase Dashboard Checklist

1. Open the Supabase dashboard.
2. Create a new project named `UptimeDesk`, or reuse an existing Supabase organization/account.
3. Save the database password in a password manager.
4. Open the project `Connect` panel.
5. Copy the `Session pooler` host, port, database name, and user.
6. Build the `DATABASE_URL` as a JDBC URL.
7. Add the values to Render environment variables when the backend is deployed.
8. Keep real credentials out of Git.

## Local Testing Checklist

When credentials are ready, test the backend against Supabase with:

```bash
cd server
SPRING_PROFILES_ACTIVE=prod DATABASE_URL="jdbc:postgresql://aws-0-your-region.pooler.supabase.com:5432/postgres?sslmode=require" DATABASE_USERNAME="postgres.your-project-ref" DATABASE_PASSWORD="your-database-password" SUPABASE_JWT_ISSUER="https://your-project-ref.supabase.co/auth/v1" SUPABASE_JWT_JWK_SET_URI="https://your-project-ref.supabase.co/auth/v1/.well-known/jwks.json" ./mvnw test
```

This test will fully pass after production-safe migrations exist. Until Flyway or Liquibase is added, `ddl-auto=validate` can fail against an empty Supabase database because Hibernate will not create tables in production mode.

## Notes

- Do not use the Supabase service role key in the frontend.
- Do not commit `.env` files with real values.
- `server/.env.example` is safe to commit because it contains placeholders only.
- The next backend task is adding Flyway or Liquibase migrations so production schema creation is controlled.
