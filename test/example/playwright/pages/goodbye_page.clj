(ns example.playwright.pages.goodbye-page
  (:require [example.playwright.test-base :as base]))

(def page-path "/goodbye")

(def selectors
  {:heading "h1"
   :main-content "main"
   :body "body"})

(defn navigate []
  (base/navigate-to page-path))

(defn get-heading-text []
  (base/text-content (:heading selectors)))

(defn get-main-content []
  (base/text-content (:main-content selectors)))

(defn page-loaded? []
  (and (base/visible? (:heading selectors))
       (base/visible? (:body selectors))))

(defn wait-for-page-load []
  (base/wait-for-selector (:heading selectors))
  (page-loaded?))

(defn get-all-text []
  (base/text-content (:body selectors)))