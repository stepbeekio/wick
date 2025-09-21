# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Wick - Clojure Web Application

A Clojure web application template with full-stack capabilities including authentication, background jobs, and reactive frontend using Datastar.

## Build & Development Commands

### Core Development Commands
```bash
just run                    # Run the application
just test                   # Run all tests with Kaocha
just test-e2e              # Run Playwright end-to-end tests
just format                # Format code with cljfmt
just lint                  # Lint code with clj-kondo
just build-fe              # Build frontend assets (CSS + ClojureScript)
just outdated              # Check for outdated dependencies
```

### Test Commands
```bash
# Run all tests
just test

# Run specific test namespace
clojure -M:test -m kaocha.runner --focus example.namespace-test

# Run tests in watch mode
clojure -M:test -m kaocha.runner --watch

# Run with specific Timbre log level (for debugging auth tests)
TIMBRE_LEVEL=:debug clojure -M:test --focus example.auth.service-test

# End-to-end tests
DEBUG=1 just test-e2e
```

### Development REPL Workflow
1. Start dependencies: `docker-compose up -d`
2. Start REPL: `clojure -M:dev` or use editor integration
3. In REPL, evaluate `dev/user.clj`:
   - `(start-system!)` - Starts server, shadow-cljs, CSS watcher, and Docker dependencies
   - `(stop-system!)` - Stops the system
   - `(restart-system!)` - Restarts everything
   - `(db)` - Access database connection
   - `(env)` - Access environment configuration

### Frontend Development
- **CSS**: Tailwind v4 with automatic rebuilding via `(watch-css)` in REPL
- **ClojureScript**: Shadow-cljs with Stimulus controllers
- **Build command**: `npm run css:build && npx shadow-cljs release frontend`
- **Output**: `assets/public/js/main.js` and `assets/public/css/main.css`

## Architecture Overview

### System Components (`src/example/system.clj`)
The application uses a component-based architecture with lifecycle management:
- **Environment**: Dotenv configuration loader
- **Database**: HikariCP connection pool with PostgreSQL
- **Worker**: Proletarian background job processor
- **Server**: HTTP-Kit web server
- **Cookie Store**: Session management

### Three-Layer Architecture Pattern
The codebase follows a separation of concerns pattern:

1. **Routes Layer** (`*/routes.clj`)
   - HTTP request/response handling
   - Middleware application
   - Response formatting

2. **Service Layer** (`*/service.clj`)
   - Business logic
   - Database queries
   - Authentication logic
   - Returns data maps (not HTTP responses)

3. **Views Layer** (`*/views.clj`)
   - Hiccup template rendering
   - Pure functions taking data maps
   - Datastar SSE integration

### Key Modules

#### Authentication System (`src/example/auth/`)
- **Service**: User registration, login, password hashing (buddy-hashers)
- **Middleware**: Session-based authentication checks
- **Views**: Login, signup, logout forms
- **Routes**: Auth endpoints with CSRF protection
- **Utils**: Helper functions for auth checks

#### Routes & Middleware (`src/example/`)
- **routes.clj**: Main router using Reitit, 404 handling
- **middleware.clj**: Security headers, CSRF, sessions, authentication

#### Database (`res/db/migration/`)
- Flyway migrations (auto-run on startup)
- V01: Core schema
- V02: Proletarian job queue tables
- V03: Authentication tables (users, sessions)

#### Background Jobs (`src/example/jobs.clj`)
- Proletarian worker configuration
- JSON serialization for job payloads
- Automatic retry with exponential backoff

#### Frontend (`src/example/frontend/`)
- **app.cljs**: Main entry point, Stimulus initialization
- **stimulus/**: Controllers for interactive components
- Datastar integration for reactive SSE updates

### Testing Infrastructure

#### Test System (`test/example/test_system.clj`)
- TestContainers PostgreSQL for integration tests
- Automatic database creation/teardown
- Migrations run in test environment
- Mock system components

#### Test Patterns
```clojure
;; Route test with mock system
(deftest route-test
  (test-system/with-system [sys (test-system/create-test-system)]
    (let [handler (get-handler sys)
          response (handler (mock/request :get "/path"))]
      (is (= 200 (:status response))))))

;; Database integration test
(deftest db-test
  (test-system/with-database [db]
    ; Test with isolated database
    ))
```

## Important Implementation Notes

### Route Refactoring Strategy
When refactoring routes, follow this pattern:
1. Extract database logic to service namespace
2. Extract view rendering to views namespace
3. Keep route handler thin - only orchestration

### Authentication
- Session-based (cookie store)
- Passwords hashed with buddy-hashers (bcrypt+sha512)
- CSRF protection on all POST routes
- Auth middleware available for protected routes

### Database Access
- Use next.jdbc for queries
- HoneySQL for dynamic query generation
- Connection pool managed by HikariCP
- TestContainers for test isolation

### Frontend Integration
- Stimulus controllers for JavaScript behavior
- Datastar for server-sent events (SSE)
- Hiccup for HTML generation
- Tailwind CSS v4 for styling

## Environment Configuration
Uses `.env` file with Dotenv:
- `POSTGRES_URL`, `POSTGRES_USERNAME`, `POSTGRES_PASSWORD`
- `PORT` - Server port
- `ENVIRONMENT` - development/production
- Session keys and other secrets

## Development Tips
- Always run `just lint` and `just format` before committing
- Use `test-system/with-system` for integration tests
- Keep services pure - no HTTP concerns
- Views should be pure functions
- Use REPL-driven development with `dev/user.clj`
- CSS changes trigger automatic rebuild in development