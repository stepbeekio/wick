(ns example.hello.playwright-database-test
  (:require [clojure.test :refer [deftest testing is]]
            [example.playwright.pages.hello-page :as hello-page]
            [example.playwright.test-base :as base]
            [next.jdbc :as jdbc]))

(deftest ^:playwright hello-page-database-test
  (testing "Hello page displays data from database"
    (base/with-playwright-system

      (testing "Page loads and shows database content"
        (hello-page/navigate)
        (is (hello-page/wait-for-page-load))
        (is (hello-page/has-database-content?)
            "Page should display content from database query"))

      (testing "Database content is consistent"
        (let [db-result (jdbc/execute-one! (:example.system/db base/*system*)
                                           ["SELECT 'earth' as planet"])
              page-content (hello-page/get-all-text)]
          (is (not (nil? db-result)))
          (is (= "earth" (:planet db-result)))
          (is (re-find #"(?i)earth" page-content)
              "Database result should appear on page"))))))

(deftest ^:playwright database-state-isolation-test
  (testing "Each test gets a fresh database"
    (base/with-playwright-system

      (testing "First test run - insert test data"
        (jdbc/execute! (:example.system/db base/*system*)
                       ["CREATE TABLE IF NOT EXISTS test_table (id SERIAL PRIMARY KEY, name TEXT)"])
        (jdbc/execute! (:example.system/db base/*system*)
                       ["INSERT INTO test_table (name) VALUES (?)" "test-data-1"])
        (let [result (jdbc/execute-one! (:example.system/db base/*system*)
                                        ["SELECT COUNT(*) as count FROM test_table"])]
          (is (= 1 (:count result)))))

      (testing "Page still works after database operations"
        (hello-page/navigate)
        (is (hello-page/page-loaded?)))))

  (base/with-playwright-system

    (testing "Second test run - verify clean database"
      (let [table-exists (jdbc/execute-one! (:example.system/db base/*system*)
                                            ["SELECT EXISTS (
                                                SELECT FROM information_schema.tables 
                                                WHERE table_name = 'test_table'
                                               ) as exists"])]
        (is (false? (:exists table-exists))
            "Test table should not exist in new test run")))))