# Playwright E2E Tests

This directory contains end-to-end tests using Playwright with Java bindings.

## Structure

```
playwright/
├── test_base.clj              # Core test infrastructure and helpers
├── config.clj                  # Test configuration for different environments
├── navigation_test.clj         # Basic navigation and smoke tests
├── database_interaction_test.clj # Tests involving database operations
└── pages/                      # Page Object Model
    ├── hello_page.clj          # Hello page object
    └── goodbye_page.clj        # Goodbye page object
```

## Running Tests

```bash
# Run all tests including Playwright E2E tests
just test

# Run only Playwright E2E tests
just test-e2e

# Run with visible browser (for debugging)
PWDEBUG=1 just test-e2e

# Run specific test file
clojure -M:test --focus example.playwright.navigation-test
```

## Key Features

1. **Automatic System Lifecycle**: Tests automatically start the full application with a test database
2. **Page Object Model**: Reusable page objects for maintainable tests
3. **Database Isolation**: Each test gets a fresh database via TestContainers
4. **Screenshot Support**: Automatic screenshots on failure or manual for debugging
5. **Configuration**: Different configs for CI, debugging, and local development

## Writing New Tests

### Basic Test Structure

```clojure
(deftest my-new-test
  (testing "Description of test"
    (base/with-playwright-system
      ; Your test code here
      (base/navigate-to "/some-path")
      (is (base/visible? "h1")))))
```

### Using Page Objects

```clojure
(deftest page-object-test
  (base/with-playwright-system
    (hello-page/navigate)
    (is (hello-page/page-loaded?))
    (is (= "Expected Text" (hello-page/get-heading-text)))))
```

### Database Interaction

```clojure
(deftest database-test
  (base/with-playwright-system
    ; Access database via base/*system*
    (let [result (jdbc/execute-one! (:example.system/db base/*system*)
                                    ["SELECT * FROM table"])]
      ; Test with result
      )))
```

## Debugging

1. **Visual Debugging**: Set `PWDEBUG=1` to run with visible browser
2. **Screenshots**: Use `(base/screenshot "path/to/screenshot.png")`
3. **Slow Motion**: Debug config includes 500ms delay between actions
4. **Traces**: Enable traces in CI for post-mortem debugging

## CI Integration

Tests automatically run in headless mode when `CI` environment variable is set.
Traces are enabled in CI for debugging failures.

## Dependencies

- Playwright Java: 1.49.0
- TestContainers PostgreSQL: for database isolation
- Kaocha: test runner integration

## Common Issues

1. **Port conflicts**: System automatically finds available port
2. **Browser installation**: Playwright downloads browsers on first run
3. **Timeouts**: Adjust in `config.clj` if needed
4. **Database migrations**: Run automatically on system start