(ns example.cave.jobs
  (:require [example.system :as-alias system]
            [honey.sql :as sql]
            [next.jdbc :as jdbc])
  (:import (java.util UUID)))

(set! *warn-on-reflection* true)

(defn process-cave-insert
  [{::system/keys [db]} _job-type payload]
  (jdbc/execute!
   db
   (sql/format
    {:insert-into :prehistoric/hominid
     :values [{:name    "Grunk"
               :cave_id (UUID/fromString (:id payload))}]})))

(defn handlers
  []
  {:prehistoric.cave/insert #'process-cave-insert})