(ns example.goodbye.routes
  (:require [example.page-html.core :as page-html]
            [hiccup2.core :as hiccup]))

(defn goodbye-handler
  [_system _request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str
          (hiccup/html
           (page-html/view :body [:h1 "Goodbye, world"])))})

(defn routes
  [system]
  [["/goodbye" {:get {:handler (partial #'goodbye-handler system)}}]])