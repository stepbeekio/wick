(ns example.cave.routes
  (:require [example.middleware :as middleware]
            [example.system :as-alias system]
            [hiccup2.core :as hiccup]
            [next.jdbc :as jdbc]
            [ring.util.anti-forgery :as anti-forgery]
            [ring.util.response :as response]))

(defn cave-create-handler
  [{::system/keys [db]} request]
  (let [{:keys [description]} (:params request)]
    (jdbc/execute!
     db
     ["INSERT INTO prehistoric.cave(description) VALUES (?)"
      description])
    (response/redirect "/cave")))

(defn cave-handler
  [{::system/keys [db]} _request]
  (let [caves (jdbc/execute!
               db
               ["SELECT id, description FROM prehistoric.cave"])]
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    (str
               (hiccup/html
                [:html
                 [:body
                  [:h1 "Existing caves"]
                  [:ul
                   (for [cave caves]
                     [:li (:cave/id cave) " - " (:cave/description cave)])]
                  [:h1 "Create a new cave"]
                  [:form {:method "post"
                          :action "/cave/create"}
                   (hiccup/raw (anti-forgery/anti-forgery-field))
                   [:label {:for "description"} "Description"]
                   [:input {:name "description" :type "text"}]
                   [:input {:type "submit"}]]]]))}))

(defn routes
  [system]
  [""
   {:middleware (middleware/standard-html-route-middleware system)}
   ["/cave" {:get {:handler (partial #'cave-handler system)}}]
   ["/cave/create" {:post {:handler (partial #'cave-create-handler system)}}]])