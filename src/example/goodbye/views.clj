(ns example.goodbye.views
  (:require [example.page-html.core :as page-html]
            [hiccup2.core :as hiccup]))

(defn goodbye-page
  "Renders the goodbye page"
  []
  (str
   (hiccup/html
    (page-html/view :body [:h1 "Goodbye, world"]))))