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
     :body
     (page-html/render
      (page-html/view {:body [:h1 {:stimulus/controller "greet"} (str "Hello, " planet)
                              [:form
                               [:input {:type "text" :stimulus/target {:target "name" :controller "greet"}
                                        :stimulus/action (page-html/s-> "input" "greet" "updateName") :placeholder "My name..." :required true}]
                               [:button {:type "button" :stimulus/action (page-html/s-> "click" "greet" "greet")} "Greet"]]
                              [:p {:stimulus/target {:target "output" :controller "greet"}}]]}))}))

(comment (:stimulus/action (page-html/s-> "click" "greet" "updateName")))

(def desired
  [:div {:stimulus/controller "greet"}
   [:p {:stimulus/greet-target "output"}]
   [:button {:stimulus/greet-action {:event "input" :method "update"}}]])

(defn routes
  [system]
  [["/" {:get {:handler (partial #'hello-handler system)}}]])
