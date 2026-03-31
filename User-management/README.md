# User Service

## What this includes
- Email/password registration and login (session-based)
- Google OAuth2 login (creates an account on first sign-in)
- MySQL persistence (configured in `application.properties`)

## Requirements
- Java 17
- MySQL running locally
- Google OAuth credentials (optional for Google login)

## Quick start
```bash
./mvnw spring-boot:run
```

## Google OAuth setup
Set environment variables before running:
```bash
export GOOGLE_CLIENT_ID="your-client-id"
export GOOGLE_CLIENT_SECRET="your-client-secret"
```

Then visit:
- `http://localhost:8081/login/oauth2/code/google`
- Or start login at `http://localhost:8081/oauth2/authorization/google`

## Password reset email (Gmail)
Set these environment variables before running:
```bash
export GMAIL_USERNAME="your@gmail.com"
export GMAIL_APP_PASSWORD="your-app-password"
export RESET_FROM_EMAIL="no-reply@yourdomain.com"
```
Then restart the app and use:
- `POST /auth/forgot-password`
- `POST /auth/reset-password`

## Auth endpoints
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/forgot-password`
- `POST /auth/reset-password`

Example register:
```bash
curl -i -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","password":"secret123"}'
```

Example login:
```bash
curl -i -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"secret123"}'
```

Example forgot password:
```bash
curl -i -X POST http://localhost:8081/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com"}'
```

Example reset password:
```bash
curl -i -X POST http://localhost:8081/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"token":"<token>","newPassword":"newSecret123"}'
```
