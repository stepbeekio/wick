(ns example.goodbye.views-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as string]
            [example.goodbye.views :as views]))

(deftest goodbye-page-test
  (testing "goodbye-page renders correct HTML"
    (let [result (views/goodbye-page)]
      (is (string? result))
      (is (string/includes? result "Goodbye, world"))
      (is (string/includes? result "<h1"))
      (is (string/includes? result "</h1>"))
      (is (string/includes? result "<html"))
      (is (string/includes? result "</html>")))))