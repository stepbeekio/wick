(ns example.routes
  (:require [clojure.tools.logging :as log]
            [example.counter.routes :as counter-routes]
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
   (counter-routes/routes system)
   [["/assets/*" (reitit-ring/create-resource-handler)]]])

(comment
  (require '[clojure.java.io :as io])
  (io/resource "assets/js/main.js"))

(defn not-found-handler
  [request]
  {:status 404
   :headers {"Content-Type" "text/html; charset=UTF-8"}
   :body (str
          (hiccup/html
           [:html
            [:head
             [:meta {:charset "UTF-8"}]
             [:title "Page Not Found - Wick"]
             [:meta {:name "viewport"
                     :content "width=device-width, initial-scale=1.0"}]
             [:script {:src "/assets/js/main.js"}]
             [:link {:href "/assets/css/main.css" :rel "stylesheet"}]]
            [:body
             [:div {:class "min-h-screen bg-gray-50 flex items-center justify-center p-6"}
              [:div {:class "max-w-md w-full bg-white rounded-lg shadow-lg border border-gray-200 p-8 text-center"}
               [:div {:class "mb-6"}
                [:h1 {:class "text-6xl font-bold mb-2 text-gray-800"} "404"]
                [:div {:class "text-2xl font-semibold mb-4 text-gray-700"} "Page Not Found"]]
               [:div {:class "mb-8 text-gray-600"}
                [:p {:class "text-lg mb-2"} "The page you're looking for doesn't exist."]
                [:p {:class "text-sm"}
                 "The URL " [:code {:class "bg-gray-100 px-2 py-1 rounded text-xs text-gray-800"} (:uri request)] " could not be found."]]
               [:div {:class "space-y-3"}
                [:button {:onclick "window.history.back()"}
                 :class "w-full bg-gray-100 hover:bg-gray-200 transition-colors duration-200 px-6 py-3 rounded-md font-medium text-gray-700 border border-gray-300"
                 "← Go Back"]
                [:a {:href "/"
                     :class "block w-full bg-gray-800 hover:bg-gray-900 transition-colors duration-200 px-6 py-3 rounded-md font-medium text-white"}
                 "Return Home"]]]]]]))})

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



