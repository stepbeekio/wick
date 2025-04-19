(ns example.test-system
  (:require [clojure.java.io :as io]
            [next.jdbc :as jdbc]
            [example.database.migrations :as migrations])
  (:import (java.util Properties)
           (org.testcontainers.containers PostgreSQLContainer)
           (org.testcontainers.containers.wait.strategy Wait)
           (org.testcontainers.utility DockerImageName)))

(set! *warn-on-reflection* true)

(defn ^:private start-pg-test-container
  []
  (let [container (PostgreSQLContainer.
                   (-> (DockerImageName/parse "postgres")
                       (.withTag "17")))]
    (.start container)
    (.waitingFor container (Wait/forListeningPort))
    container))

(defonce ^:private pg-test-container-delay
  (delay
    (let [container (start-pg-test-container)]
      (.addShutdownHook (Runtime/getRuntime)
                        (Thread. #(PostgreSQLContainer/.close container)))
      container)))

(defn ^:private get-test-db
  []
  (let [container @pg-test-container-delay]
    (jdbc/get-datasource
     {:dbtype   "postgresql"
      :jdbcUrl  (str "jdbc:postgresql://"
                     (PostgreSQLContainer/.getHost container)
                     ":"
                     (PostgreSQLContainer/.getMappedPort container PostgreSQLContainer/POSTGRESQL_PORT)
                     "/"
                     (PostgreSQLContainer/.getDatabaseName container)
                     "?user="
                     (PostgreSQLContainer/.getUsername container)
                     "&password="
                     (PostgreSQLContainer/.getPassword container))})))

(def ^:private test-counter (atom 0))

(defn with-test-db
  [callback]
  (let [test-database-name (str "test_" (swap! test-counter inc))
        container          @pg-test-container-delay
        db                 (get-test-db)]
    (jdbc/execute!
     db
     [(format "CREATE DATABASE %s TEMPLATE %s;"
              test-database-name
              (PostgreSQLContainer/.getDatabaseName container))])

    (try
      (let [username (PostgreSQLContainer/.getUsername container)
            password (PostgreSQLContainer/.getPassword container)
            jdbcUrl (str "jdbc:postgresql://"
                         (PostgreSQLContainer/.getHost container)
                         ":"
                         (PostgreSQLContainer/.getMappedPort container
                                                             PostgreSQLContainer/POSTGRESQL_PORT)
                         "/"
                         test-database-name
                         )
            db (jdbc/get-datasource
                {:dbtype   "postgresql"
                 :jdbcUrl (str jdbcUrl "?user=" username "&password=" password) })]
        (migrations/run-migrations {:jdbcUrl jdbcUrl :username username :password password})
        (callback db))
      (finally
        (jdbc/execute! db
                       [(format "DROP DATABASE %s;" test-database-name)])))))
