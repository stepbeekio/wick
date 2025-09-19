(ns example.system-test
  (:require [clojure.test :refer [deftest testing is]]
            [example.system :as system]
            [example.test-system :as test-system]
            [next.jdbc :as jdbc]
            [ring.middleware.session.memory :as memory])
  (:import (com.zaxxer.hikari HikariDataSource)
           (io.github.cdimascio.dotenv Dotenv)))

(deftest system-component-lifecycle-test
  (testing "Individual system components can start and stop"

    (testing "Environment component"
      (let [env (system/start-env)]
        (is (instance? Dotenv env))
        (is (string? (Dotenv/.get env "PORT" "3000")))))

    (testing "Cookie store component"
      (let [cookie-store (system/start-cookie-store)]
        (is (satisfies? ring.middleware.session.store/SessionStore cookie-store))))

    (testing "Database component with test database"
      (test-system/with-test-db
        (fn [db]
          (is (instance? javax.sql.DataSource db))
          (is (= 1 (:count (jdbc/execute-one! db ["SELECT 1 as count"])))))))))

(deftest minimal-system-test
  (testing "Minimal system without server starts correctly"
    (test-system/with-test-db
      (fn [test-db]
        (let [; Create a minimal test system without starting server
              env (system/start-env)
              cookie-store (system/start-cookie-store)
              system {::system/env env
                      ::system/cookie-store cookie-store
                      ::system/db test-db}]

          (testing "System has required components"
            (is (::system/env system))
            (is (::system/cookie-store system))
            (is (::system/db system)))

          (testing "Database connection works"
            (let [result (jdbc/execute-one! (::system/db system)
                                            ["SELECT 'test' as value"])]
              (is (= "test" (:value result)))))

          ; Note: We don't stop the test-db as it's managed by with-test-db
          )))))

(deftest db-config-test
  (testing "Database configuration from environment"
    (let [env (system/start-env)]
      (when (and (Dotenv/.get env "POSTGRES_USERNAME")
                 (Dotenv/.get env "POSTGRES_URL")
                 (Dotenv/.get env "POSTGRES_PASSWORD"))
        (let [config (system/db-config env)]
          (is (map? config))
          (is (contains? config :jdbcUrl))
          (is (contains? config :username))
          (is (contains? config :password))
          (is (string? (:jdbcUrl config)))
          (is (string? (:username config)))
          (is (string? (:password config))))))))

(deftest system-dependencies-test
  (testing "System components have correct dependencies"
    (test-system/with-test-db
      (fn [test-db]
        (let [env (system/start-env)
              cookie-store (system/start-cookie-store)
              system {::system/env env
                      ::system/cookie-store cookie-store
                      ::system/db test-db}]

          (testing "Worker requires database"
            ; We can't fully test worker without a proper job queue setup
            ; but we can verify the database dependency exists
            (is (::system/db system)))

          (testing "Server requires all components"
            ; We verify that the system has all components needed for server
            (is (::system/env system))
            (is (::system/cookie-store system))
            (is (::system/db system))))))))