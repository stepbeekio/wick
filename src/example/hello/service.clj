(ns example.hello.service
  (:require [next.jdbc :as jdbc]))

(defn get-planet-info
  "Fetches planet information from the database"
  [db]
  (jdbc/execute-one! db ["SELECT 'earth' as planet"]))