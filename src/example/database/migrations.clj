(ns example.database.migrations
  (:import [org.flywaydb.core Flyway]
           [org.flywaydb.core.api.configuration FluentConfiguration]))


(defn run-migrations
  "Run Flyway migrations against the specified database"
  [config]
  (tap> config)
  (let [{jdbcUrl :jdbcUrl username :username password :password} config] 
    (-> (new FluentConfiguration)
        (.dataSource jdbcUrl username password)
        ;; Configure the location where migration files are stored
        (.locations (into-array String ["classpath:db/migration"]))
        ;; Build the Flyway instance
        (.load)
        ;; Run the migrations
        (.migrate))))

