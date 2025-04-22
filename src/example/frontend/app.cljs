(ns example.frontend.app
  (:require [example.frontend.stimulus.core :as stimulus]))

(defn init []
  (stimulus/init)
  (println "With stimulus"))
