(ns example.test-system
  (:require [clojure.java.io :as io]
            [next.jdbc :as jdbc])
  (:import (java.util Properties)
           (org.apache.ibatis.migration DataSourceConnectionProvider FileMigrationLoader)
           (org.apache.ibatis.migration.operations UpOperation)
           (org.apache.ibatis.migration.options DatabaseOperationOption)
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

(defn ^:private run-migrations
  [db]
  (let [scripts-dir (io/file "migrations/scripts")
        env-properties (io/file "migrations/environments/development.properties")]
    (with-open [env-properties-stream (io/input-stream env-properties)]
      (.operate (UpOperation.)
                (DataSourceConnectionProvider. db)
                (FileMigrationLoader.
                 scripts-dir
                 "UTF-8"
                 (doto (Properties.)
                   (.load env-properties-stream)))
                (doto (DatabaseOperationOption.)
                  (.setSendFullScript true))
                nil))))

(defonce ^:private migrations-delay
  (delay (run-migrations (get-test-db))))

(def ^:private test-counter (atom 0))

(defn with-test-db
  [callback]
  @migrations-delay
  (let [test-database-name (str "test_" (swap! test-counter inc))
        container          @pg-test-container-delay
        db                 (get-test-db)]
    (jdbc/execute!
     db
     [(format "CREATE DATABASE %s TEMPLATE %s;"
              test-database-name
              (PostgreSQLContainer/.getDatabaseName container))])

    (try
      (let [db (jdbc/get-datasource
                {:dbtype   "postgresql"
                 :jdbcUrl  (str "jdbc:postgresql://"
                                (PostgreSQLContainer/.getHost container)
                                ":"
                                (PostgreSQLContainer/.getMappedPort container
                                                                    PostgreSQLContainer/POSTGRESQL_PORT)
                                "/"
                                test-database-name
                                "?user="
                                (PostgreSQLContainer/.getUsername container)
                                "&password="
                                (PostgreSQLContainer/.getPassword container))})]
        (callback db))
      (finally
        (jdbc/execute! db
                       [(format "DROP DATABASE %s;" test-database-name)])))))