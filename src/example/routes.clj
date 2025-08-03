(ns example.routes
  (:require [clojure.tools.logging :as log]
            [example.goodbye.routes :as goodbye-routes]
            [example.hello.routes :as hello-routes]
            [example.static.routes :as static-routes]
            [example.system :as-alias system]
            [hiccup2.core :as hiccup]
            [reitit.ring :as reitit-ring]))

(defn routes
  [system]
  [""
   (static-routes/routes system)
   (hello-routes/routes system)
   (goodbye-routes/routes system)
   [["/assets/*" (reitit-ring/create-resource-handler)]]])

(comment
  (require '[clojure.java.io :as io])
  (io/resource "assets/js/main.js"))

(defn not-found-handler
  [_request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (str
          (hiccup/html
           [:html
            [:body
             [:h1 "Not Found"]]]))})

(defn root-handler
  ([system request]
   ((root-handler system) request))
  ([system]
   (let [handler (reitit-ring/ring-handler
                  (reitit-ring/router
                   (routes system))
                  #'not-found-handler)]
     (fn root-handler [request]
       (log/info (str (:request-method request) " - " (:uri request)))
       (handler request)))))



