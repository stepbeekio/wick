(ns example.goodbye.views
  (:require [example.page-html.core :as page-html]))

(defn goodbye-page
  "Renders the goodbye page"
  []
  (page-html/render
   (page-html/view
    {:title "Goodbye"
     :body [:h1 "Goodbye, world"]})))