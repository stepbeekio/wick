(ns example.main
  (:require [example.system :as system]))

(defn -main []
  (system/start-system))