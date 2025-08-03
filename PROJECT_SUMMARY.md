# Project Summary: Wick

## Overview
Wick is a full-stack Clojure web application built with a modern, functional architecture. It features a Ring-based HTTP server with Reitit routing, PostgreSQL database integration with HikariCP connection pooling, background job processing with Proletarian, and ClojureScript frontend components using Stimulus controllers. The application follows a clean system lifecycle approach with proper startup/shutdown procedures.

## Architecture

### System Lifecycle
The application uses a centralized system management approach in `example.system`:
- **Environment**: Loads configuration from `.env` files using dotenv-java
- **Database**: PostgreSQL with HikariCP connection pooling and Flyway migrations
- **Background Jobs**: Proletarian worker system for async job processing  
- **HTTP Server**: Jetty server with Ring handlers and middleware
- **Development**: Conditional handler reloading in development mode

### Request Flow
1. Jetty server receives HTTP requests
2. Ring middleware stack processes requests (security, params, sessions, etc.)
3. Reitit router matches routes and dispatches to handlers
4. Handlers generate responses using Hiccup for HTML
5. Static assets served from resources with resource handler

## Key File Structure

### Core Application Files
- **`src/example/main.clj`** - Application entry point with `-main` function
- **`src/example/system.clj`** - System lifecycle management (start/stop components)
- **`src/example/routes.clj`** - Main routing configuration using Reitit
- **`src/example/middleware.clj`** - Ring middleware stack definition

### Feature Modules
- **`src/example/hello/routes.clj`** - Hello world route handlers
- **`src/example/goodbye/routes.clj`** - Goodbye route handlers  
- **`src/example/static/routes.clj`** - Static asset route configuration
- **`src/example/page_html/core.clj`** - HTML page generation utilities
- **`src/example/jobs.clj`** - Background job definitions and processing

### Database & Migrations
- **`src/example/database/migrations.clj`** - Flyway database migration runner

### Frontend Components
- **`src/example/frontend/app.cljs`** - Main ClojureScript application
- **`src/example/frontend/stimulus/core.cljs`** - Stimulus framework integration
- **`src/example/frontend/stimulus/controller/greetcontroller.cljs`** - Sample Stimulus controller
- **`assets/public/js/manifest.edn`** - Frontend asset manifest

### Testing
- **`test/example/test_system.clj`** - System-level test utilities
- **`test/example/page_html/core_test.clj`** - HTML generation tests
- **`test/example/math_test.clj`** - Sample unit tests

## Dependencies & Tools

### Core Web Stack
- **Ring 1.13.0** - HTTP server abstraction and middleware
- **Reitit 0.7.2** - Fast, data-driven router for Ring
- **Hiccup 2.0.0-RC3** - HTML generation from Clojure data structures
- **ring-defaults 0.5.0** - Sensible Ring middleware defaults
- **ring-refresh 0.2.0** - Auto browser refresh for development ⚠️ *Not yet integrated*

### Database & Persistence  
- **next.jdbc 1.3.955** - Modern JDBC wrapper for Clojure
- **PostgreSQL 42.7.4** - PostgreSQL JDBC driver
- **HikariCP 6.0.0** - High-performance connection pool
- **HoneySQL 2.6.1203** - DSL for generating SQL from Clojure data
- **Flyway 11.7.2** - Database migration tool

### Background Processing
- **Proletarian 1.0.86-alpha** - PostgreSQL-backed job queue for Clojure

### Frontend & Assets
- **Shadow-CLJS 2.28.18** - ClojureScript build tool (dev dependency)
- **Beholder 1.0.2** - File watching for CSS builds (dev dependency)

### Development & Testing
- **nREPL 1.3.0** - Interactive development server (port 7888)
- **Kaocha 1.91.1392** - Comprehensive test runner
- **TestContainers 1.20.3** - Integration testing with Docker PostgreSQL
- **cljfmt 0.13.0** - Code formatting tool (:format alias)
- **clj-kondo 2024.09.27** - Linting tool (:lint alias)

### Utilities
- **Cheshire 5.13.0** - JSON encoding/decoding
- **dotenv-java 3.0.2** - Environment variable management
- **tools.logging 1.3.0** - Logging facade with slf4j-simple 2.0.16

## Available Commands

### Development
```bash
# Start development server with nREPL
clj -M:dev

# Format code
clj -M:format

# Lint code  
clj -M:lint

# Run tests
clj -M:dev -m kaocha.runner
```

### Production
```bash
# Run application
clj -M -m example.main
```

## Implementation Patterns & Conventions

### System Management
- Uses qualified keywords with `::` for system component keys
- Implements proper start/stop lifecycle with resource cleanup
- Environment-aware handler construction (development vs production)

### Request Handling
- Middleware applied as vector of functions in `example.middleware`
- Routes defined as nested data structures for Reitit
- Handlers receive system context for database/worker access
- Comprehensive security middleware (CSRF, XSS, clickjacking protection)

### Database Access
- Connection pooling with HikariCP for production scalability
- Migration-first approach using Flyway on startup
- Modern next.jdbc for database interactions

### Frontend Integration  
- ClojureScript compiled with Shadow-CLJS
- Stimulus controllers for progressive enhancement
- Asset manifest for production builds

### Background Jobs
- PostgreSQL-backed job queue using Proletarian
- JSON serialization for job payloads
- System context passed to job processors

## Development Workflow

### Getting Started
1. Set up `.env` file with database credentials and configuration
2. Start PostgreSQL database
3. Run `clj -M:dev` to start development server with nREPL
4. Connect to nREPL on port 7888 for interactive development
5. Access application at configured PORT

### Development Features
- **Hot Reloading**: Development mode uses var indirection for handler reloading
- **Auto Refresh**: ring-refresh dependency available but not yet integrated
- **File Watching**: Beholder watches for CSS changes
- **Interactive Development**: nREPL server for REPL-driven development

### Testing Strategy
- TestContainers for integration tests with real PostgreSQL
- Kaocha as test runner with comprehensive reporting  
- Separate test system setup in `test/example/test_system.clj`

## Extension Points

### Adding New Routes
1. Create new route namespace following `example.hello.routes` pattern
2. Add route to main routes vector in `example.routes/routes`
3. Routes receive system context for database/worker access

### Database Changes
1. Add Flyway migration SQL files to resources
2. Migrations run automatically on system startup
3. Use next.jdbc and HoneySQL for queries

### Background Jobs
1. Define job processing functions in `example.jobs`
2. Jobs receive system context and serialized payloads
3. Use Proletarian worker API to enqueue jobs

### Frontend Components
1. Add new Stimulus controllers following existing pattern
2. Update Shadow-CLJS configuration for new build targets
3. Use Hiccup for server-side HTML generation

### Middleware Integration
1. Add new middleware functions to `example.middleware/standard-html-route-middleware` vector
2. Middleware has access to system components via destructuring
3. Follow Ring middleware patterns (functions returning functions)

## Ring-Refresh Integration Status

**Current Status**: ring-refresh is added as a dependency but not yet integrated into the middleware stack.

**Integration Required**: 
1. Add `ring.middleware.refresh/wrap-refresh` to the middleware stack in development mode
2. Consider conditional application only in development environment
3. Ensure proper ordering in middleware chain (typically near the end)

**Benefits**: Automatic browser refresh when Clojure files change, improving development experience.
