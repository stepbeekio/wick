(ns example.system
  (:require [example.database.migrations :as db-migrations]
            [example.jobs :as jobs]
            [example.routes :as routes]
            [next.jdbc.connection :as connection]
            [proletarian.worker :as worker]
            [org.httpkit.server :as http-kit]
            [ring.middleware.session.cookie :as session-cookie])
  (:import (com.zaxxer.hikari HikariDataSource)
           (io.github.cdimascio.dotenv Dotenv)))

(set! *warn-on-reflection* true)

(defn start-env
  []
  (Dotenv/load))

(defn start-cookie-store
  []
  (session-cookie/cookie-store))

(defn db-config [env]
  (let [username (Dotenv/.get env "POSTGRES_USERNAME")
        jdbcUrl (Dotenv/.get env "POSTGRES_URL")
        password (Dotenv/.get env "POSTGRES_PASSWORD")
        config     {:jdbcUrl jdbcUrl
                    :username username
                    :password password}]
    config))

(defn start-db
  [{::keys [env]}]
  (let [config (db-config env)]
    (db-migrations/run-migrations config)
    (connection/->pool HikariDataSource config)))

(defn stop-db
  [db]
  (HikariDataSource/.close db))

(defn start-worker
  [{::keys [db] :as system}]
  (let [worker (worker/create-queue-worker
                db
                (partial #'jobs/process-job system)
                {:proletarian/log #'jobs/logger
                 :proletarian/serializer jobs/json-serializer})]
    (worker/start! worker)
    worker))

(defn stop-worker
  [worker]
  (worker/stop! worker))

(defn start-server
  [{::keys [env] :as system}]
  (let [handler (if (= (Dotenv/.get env "ENVIRONMENT") "development")
                  (partial #'routes/root-handler system)
                  (routes/root-handler system))
        port (Long/parseLong (Dotenv/.get env "PORT"))]
    (http-kit/run-server handler {:port port})))

(defn stop-server
  [stop-fn]
  (when stop-fn
    (stop-fn :timeout 100)))

(defn start-system
  []
  (let [system-so-far {::env (start-env)}
        system-so-far (merge system-so-far {::cookie-store (start-cookie-store)})
        system-so-far (merge system-so-far {::db (start-db system-so-far)})
        system-so-far (merge system-so-far {::worker (start-worker system-so-far)})]
    (merge system-so-far {::server (start-server system-so-far)})))

(defn stop-system
  [system]
  (stop-server (::server system))
  (stop-worker (::worker system))
  (stop-db (::db system)))
