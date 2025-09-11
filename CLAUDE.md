# Wick Project - Development Guidelines

## Route Refactoring Plan - Separation of Concerns

### Current State
- Database queries are directly embedded in route handlers (e.g., `hello/routes.clj`)
- View rendering logic (Hiccup templates) is mixed with route handling
- This makes testing difficult and violates single responsibility principle

### Target Architecture
Three-layer separation:
1. **Routes Layer** - HTTP request/response handling only
2. **Service Layer** - Business logic and database access
3. **View Layer** - Hiccup template rendering

### Refactoring Strategy

#### Phase 1: Hello Routes (Most Complex)
1. Create `src/example/hello/service.clj`
   - Extract database query logic
   - Return data maps, not HTTP responses
   - Example: `(get-planet-info db)` returns `{:planet "earth"}`

2. Create `src/example/hello/views.clj`
   - Extract all Hiccup template logic
   - Pure functions that take data and return Hiccup
   - Example: `(render-hello-page {:planet "earth"})` returns Hiccup structure

3. Update `src/example/hello/routes.clj`
   - Call service to get data
   - Pass data to view for rendering
   - Wrap view output in HTTP response

#### Phase 2: Apply Pattern to Other Routes
- Goodbye routes (simple, no DB)
- Static routes (if any logic exists)

### Example Structure

```clojure
;; service.clj
(ns example.hello.service
  (:require [next.jdbc :as jdbc]))

(defn get-planet-info [db]
  (jdbc/execute-one! db ["SELECT 'earth' as planet"]))

;; views.clj
(ns example.hello.views
  (:require [example.page-html.core :as page-html]))

(defn hello-page [{:keys [planet]}]
  (page-html/view
    {:title "Wick"
     :body [...]}))

;; routes.clj
(ns example.hello.routes
  (:require [example.hello.service :as service]
            [example.hello.views :as views]))

(defn hello-handler [system _request]
  (let [data (service/get-planet-info (:db system))]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (views/hello-page data)}))
```

### Benefits
- **Testability**: Can test services with mock DB, views with pure data
- **Reusability**: Services can be used by multiple routes or background jobs
- **Maintainability**: Clear separation of concerns
- **Flexibility**: Easy to add caching, logging, or change view technology

### Testing Approach
- **Service tests**: Use test database, verify data returned
- **View tests**: Pure functions, test with sample data
- **Route tests**: Mock service layer, verify HTTP responses

## Testing Strategy and Information

## Testing Framework Setup
- **Test Runner**: Kaocha (v1.91.1392) - configured in `tests.edn`
- **Database Testing**: TestContainers with PostgreSQL
- **Test Command**: `just test`
- **Test Aliases**: `:dev` and `:test` in `deps.edn`

## Existing Test Infrastructure
- Test system utilities: `test/example/test_system.clj`
- Database test setup with automatic creation/teardown
- Migrations run automatically in test environment
- Existing tests:
  - `test/example/math_test.clj` - Basic unit test example
  - `test/example/page_html/core_test.clj` - Stimulus/Hiccup integration tests

## Key Modules Requiring Tests

### Backend (Clojure)
- **System** (`src/example/system.clj`) - Lifecycle, DB connections, configuration
- **Routes** (`src/example/routes.clj`) - Root handler, routing logic, 404 handling
- **Route Handlers**:
  - Hello routes (`src/example/hello/routes.clj`) - Complex with DB
  - Goodbye routes (`src/example/goodbye/routes.clj`) - Simple
  - Static routes (`src/example/static/routes.clj`) - Favicon
- **Middleware** (`src/example/middleware.clj`) - Security, sessions, CSRF
- **Jobs** (`src/example/jobs.clj`) - Background processing with Proletarian
- **Database** (`src/example/database/migrations.clj`) - Flyway migrations

### Frontend (ClojureScript)
- App initialization (`src/example/frontend/app.cljs`)
- Stimulus controllers and integration

## Test Writing Patterns

### Basic Route Test Structure
```clojure
(ns example.routes-test
  (:require [clojure.test :refer [deftest testing is]]
            [example.test-system :as test-system]
            [ring.mock.request :as mock]))

(deftest route-test
  (test-system/with-system [sys (test-system/create-test-system)]
    (let [handler (get-handler sys)
          response (handler (mock/request :get "/path"))]
      (is (= 200 (:status response))))))
```

### Database Integration Test Pattern
```clojure
(deftest db-test
  (test-system/with-database [db]
    ; Test with isolated database
    ))
```

## Important Commands
- Run tests: `just test`
- Run specific test: `clojure -M:test -m kaocha.runner --focus example.namespace-test`
- Watch mode: `clojure -M:test -m kaocha.runner --watch`

## Notes
- Each test phase should be small and focused
- Run tests after each addition to catch issues early
- Commit after each successful test addition
- TestContainers provides real PostgreSQL for integration tests
- Test system automatically handles database setup/teardown
- to memorize