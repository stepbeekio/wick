(ns example.playwright.pages.hello-page
  (:require [example.playwright.test-base :as base]))

(def page-path "/")

(def selectors
  {:heading "h1"
   :planet-info "[data-testid='planet-info']"
   :main-content "main"
   :body "body"})

(defn navigate []
  (base/navigate-to page-path))

(defn get-heading-text []
  (base/text-content (:heading selectors)))

(defn get-planet-info []
  (when (base/visible? (:planet-info selectors))
    (base/text-content (:planet-info selectors))))

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

(defn has-database-content? []
  (let [content (get-all-text)]
    (and content
         (or (re-find #"(?i)earth" content)
             (re-find #"(?i)planet" content)
             (re-find #"(?i)hello" content)))))