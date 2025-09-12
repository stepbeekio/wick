(ns example.goodbye.routes
  (:require [example.goodbye.views :as views]))

(defn goodbye-handler
  [_system _request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (views/goodbye-page)})

(defn routes
  [system]
  [["/goodbye" {:get {:handler (partial #'goodbye-handler system)}}]])