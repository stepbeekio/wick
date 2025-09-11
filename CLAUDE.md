# Wick Project - Testing Strategy and Information

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

## Testing Todo List (Incremental Approach)

### Phase 1: Basic Setup
1. Verify test framework setup - Run existing tests with 'just test'

### Phase 2: Simple Route Tests
2. Add basic route handler tests for goodbye routes (simplest endpoint)
3. Run tests and prompt for version control

### Phase 3: Static Route Tests  
4. Add route handler tests for static routes (favicon handling)
5. Run tests and prompt for version control

### Phase 4: Complex Route Tests
6. Add route handler tests for hello routes (includes database interaction)
7. Run tests and prompt for version control

### Phase 5: Middleware Tests
8. Add middleware tests for security headers and session handling
9. Run tests and prompt for version control

### Phase 6: System Integration Tests
10. Add system integration tests for startup/shutdown lifecycle
11. Run tests and prompt for version control

### Phase 7: Background Jobs Tests
12. Add background job processing tests
13. Run tests and prompt for version control

### Phase 8: Database Tests
14. Add database migration and connection pool tests
15. Run tests and prompt for version control

### Phase 9: Error Handling Tests
16. Add error handling tests (404s, exceptions)
17. Run tests and prompt for version control

### Phase 10: Coverage Review
18. Review test coverage and identify any remaining gaps

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