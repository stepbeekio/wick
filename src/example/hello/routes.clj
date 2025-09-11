(ns example.hello.routes
  (:require [example.hello.service :as service]
            [example.hello.views :as views]
            [example.system :as-alias system]))

(defn hello-handler
  [{::system/keys [db]} _request]
  (let [data (service/get-planet-info db)]
    {:status 200
     :headers {"Content-Type" "text/html; charset=UTF-8"}
     :body (views/hello-page data)}))


(defn routes
  [system]
  [["/" {:get {:handler (partial #'hello-handler system)}}]])
