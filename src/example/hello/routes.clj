(ns example.hello.routes
  (:require [example.page-html.core :as page-html]
            [example.system :as-alias system]
            [hiccup2.core :as hiccup]
            [next.jdbc :as jdbc]))

(defn hello-handler
  [{::system/keys [db]} _request]
  (let [{:keys [planet]} (jdbc/execute-one!
                          db
                          ["SELECT 'earth' as planet"])]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str
            (hiccup/html
             (page-html/view
              :body [:h1 {:data-controller "greet"} (str "Hello, " planet)
                     [:form
                      [:input {:type "text" :data-greet-target "name" :data-action "input->greet#updateName" :placeholder "My name..." :required true}]
                      [:button {:type "button" :data-action "click->greet#greet"} "Greet"]]
                     [:p {:data-greet-target "output"}]
                     ])))}))

(defn routes
  [system]
  [["/" {:get {:handler (partial #'hello-handler system)}}]])
