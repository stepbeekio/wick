(ns example.counter.routes
  (:require [clojure.tools.logging :as log]
            [example.counter.service :as service]
            [example.counter.views :as views]
            [starfederation.datastar.clojure.adapter.http-kit :as hk-gen]
            [starfederation.datastar.clojure.api :as d*]))

(defn counter-page-handler
  "Handler for the main counter page"
  [_system _request]
  {:status 200
   :headers {"Content-Type" "text/html; charset=UTF-8"}
   :body (views/counter-page {:initial-value (service/get-counter-value)})})

(defn counter-sse-handler
  "SSE handler for real-time counter updates"
  [_system request]
  (log/info "SSE connection established for counter")
  (hk-gen/->sse-response request
                         {hk-gen/on-open
                          (fn [sse-gen]
                            (service/add-sse-connection! sse-gen)
                            (d*/patch-elements! sse-gen
                                                (views/counter-value-fragment (service/get-counter-value))))

                          hk-gen/on-close
                          (fn [sse-gen]
                            (log/info "SSE connection closed for counter")
                            (service/remove-sse-connection! sse-gen))}))

(defn increment-handler
  "Handler for incrementing the counter"
  [_system _request]
  (let [new-value (service/increment-counter!)]
    (log/info (str "Counter incremented to: " new-value))
    (service/broadcast-to-all!
     (fn [conn]
       (d*/patch-elements! conn
                           (views/counter-value-fragment new-value))))
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body ""}))

(defn decrement-handler
  "Handler for decrementing the counter"
  [_system _request]
  (let [new-value (service/decrement-counter!)]
    (log/info (str "Counter decremented to: " new-value))
    (service/broadcast-to-all!
     (fn [conn]
       (d*/patch-elements! conn
                           (views/counter-value-fragment new-value))))
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body ""}))

(defn reset-handler
  "Handler for resetting the counter"
  [_system _request]
  (let [new-value (service/reset-counter!)]
    (log/info "Counter reset to 0")
    (service/broadcast-to-all!
     (fn [conn]
       (d*/patch-elements! conn
                           (views/counter-value-fragment new-value))))
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body ""}))

(defn routes
  "Counter routes configuration"
  [system]
  ["/counter"
   ["" {:get (partial counter-page-handler system)}]
   ["/sse" {:get (partial counter-sse-handler system)}]
   ["/increment" {:post (partial increment-handler system)}]
   ["/decrement" {:post (partial decrement-handler system)}]
   ["/reset" {:post (partial reset-handler system)}]])